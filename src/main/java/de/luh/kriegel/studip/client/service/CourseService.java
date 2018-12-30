package de.luh.kriegel.studip.client.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.luh.kriegel.studip.client.config.Endpoints;
import de.luh.kriegel.studip.client.config.SubPaths;
import de.luh.kriegel.studip.client.content.model.data.Course;
import de.luh.kriegel.studip.client.content.model.data.CourseNews;
import de.luh.kriegel.studip.client.content.model.data.FileRef;
import de.luh.kriegel.studip.client.content.model.data.Folder;
import de.luh.kriegel.studip.client.content.model.data.Id;
import de.luh.kriegel.studip.client.content.model.data.Semester;
import de.luh.kriegel.studip.client.content.model.file.FileRefNode;
import de.luh.kriegel.studip.client.content.model.file.FileRefTree;
import de.luh.kriegel.studip.client.download.DownloadManager;
import de.luh.kriegel.studip.client.exception.NotAuthenticatedException;

public class CourseService {

	private static final Logger log = LogManager.getLogger(CourseService.class);

	private final String TUTORIAL_IDENTIFIER = "Übung";

	private final BasicHttpClient httpClient;

	private final AuthService authService;

	private final DownloadManager downloadManager;

	private final Map<Id, Course> courseCache = new HashMap<>();

	private final ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public CourseService(BasicHttpClient httpClient, AuthService authService) {
		this.httpClient = httpClient;
		this.authService = authService;

		downloadManager = new DownloadManager(this, httpClient, new File("test").toPath());
	}

	public void close() {
		log.info("Closing CourseService");
		downloadManager.close();
		es.shutdown();
	}

	public DownloadManager getDownloadManager() {
		return downloadManager;
	}

	public Course getCourseById(Id courseId) throws NotAuthenticatedException {
		authService.checkIfAuthenticated();

		if (courseCache.containsKey(courseId)) {
			courseCache.get(courseId);
		}

		HttpResponse response;

		try {
			response = httpClient
					.get(SubPaths.API.toString() + Endpoints.COURSE.toString().replace(":course_id", courseId.asHex()));

			if (response.getStatusLine().getStatusCode() / 100 == 2) {
				String responseBody = BasicHttpClient.getResponseBody(response);
				JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

				Course course = Course.fromJson(responseJson);

				courseCache.put(courseId, course);

				return course;
			}

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	public int getAmountCourses() throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		HttpResponse response;

		try {
			response = httpClient.get(SubPaths.API.toString()
					+ Endpoints.USER_COURSES.toString().replace(":user_id", authService.getCurrentUserId().asHex())
					+ "?limit=1");
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return -1;
		}

		if (response.getStatusLine().getStatusCode() / 100 == 2) {
			String responseBody = BasicHttpClient.getResponseBody(response);
			JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

			if (responseJson.containsKey("pagination")) {
				JSONObject paginationJson = (JSONObject) responseJson.get("pagination");

				if (paginationJson.containsKey("total")) {
					return Integer.parseInt(paginationJson.get("total").toString());
				}
			}
		}

		return -1;
	}

	/**
	 * Retrieves the courses of the current semester
	 * 
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public List<Course> getAllCourses() throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		int totalAmountCourses = getAmountCourses();

		int limit = 20;

		List<Course> allCourses = new ArrayList<>();

		HttpResponse response;

		for (int offset = 0; offset < totalAmountCourses; offset += limit) {
			try {
				response = httpClient.get(SubPaths.API.toString()
						+ Endpoints.USER_COURSES.toString().replace(":user_id", authService.getCurrentUserId().asHex())
						+ "?offset=" + offset + "&limit=" + limit);
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
				return null;
			}

			if (response.getStatusLine().getStatusCode() / 100 == 2) {
				String responseBody = BasicHttpClient.getResponseBody(response);
				JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

				if (responseJson.containsKey("collection")) {
					((Map<String, JSONObject>) responseJson.get("collection")).forEach((link, courseJson) -> {

						Course course = Course.fromJson(courseJson);

						courseCache.putIfAbsent(course.getId(), course);

						allCourses.add(course);
					});
				}
			}
		}

		return allCourses;
	}

	public Map<Course, Course> getCourseTutorialMap() throws NotAuthenticatedException, ParseException {
		List<Course> allCourses = getAllCourses();

		Map<Course, Course> courseTutorialMap = new HashMap<>();

		allCourses.forEach(course -> {
			if (!course.getTitle().contains(TUTORIAL_IDENTIFIER)) {
				Course tutorial = allCourses.stream().filter(
						c -> c.getTitle().contains(course.getTitle()) && !c.getTitle().equals(course.getTitle()))
						.findFirst().orElse(null);

				courseTutorialMap.put(course, tutorial);
			}
		});

		return courseTutorialMap;
	}

	public FileRefTree getFileRefTree(Course course) throws Exception {
		authService.checkIfAuthenticated();

		HttpResponse response;
		try {
			response = httpClient.get(SubPaths.API.toString()
					+ Endpoints.COURSE_TOP_FOLDER.toString().replace(":course_id", course.getId().asHex()));
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return null;
		}

		String responseBody = BasicHttpClient.getResponseBody(response);
		JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);
		Folder folder = Folder.fromJson(responseJson);

		FileRefTree fileRefTree = new FileRefTree(folder);
		fetchAndAddFileRefsForCourseRecursively(fileRefTree.getRoot(), fileRefTree);

		return fileRefTree;
	}

	private FileRef getFileRefFromId(Id id) throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		HttpResponse response;
		try {
			response = httpClient.get(SubPaths.API + Endpoints.FILE.getPath().replace(":file_id", id.asHex()));
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return null;
		}

		String responseBody = BasicHttpClient.getResponseBody(response);
		JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

		return FileRef.fromJson(responseJson);
	}

	private Folder getFolderFromId(Id id) throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		HttpResponse response;
		try {
			response = httpClient.get(SubPaths.API + Endpoints.FOLDER.getPath().replace(":folder_id", id.asHex()));
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return null;
		}

		String responseBody = BasicHttpClient.getResponseBody(response);
		JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

		return Folder.fromJson(responseJson);
	}

	private void fetchAndAddFileRefsForCourseRecursively(FileRefNode node, FileRefTree fileRefTree) throws Exception {

		if (node.isDirectory()) {
			List<CompletableFuture<FileRef>> fileRefs = new ArrayList<>();
			// List<FileRef> fileRefs = new ArrayList<>();

			node.getFolder().getFileRefs().forEach(fileRefId -> {
				// try {
				// fileRefs.add(getFileRefFromId(fileRefId));
				// } catch (NotAuthenticatedException | ParseException e) {
				// e.printStackTrace();
				// }
				fileRefs.add(CompletableFuture.supplyAsync(() -> {
					try {
						return getFileRefFromId(fileRefId);
					} catch (NotAuthenticatedException | ParseException e) {
						e.printStackTrace();
					}
					return null;
				}, es));
			});
			// });

			// for (CompletableFuture<FileRef> cfFileRef : fileRefs) {
			// FileRefNode fileRefNode = fileRefTree.createFileNode(cfFileRef.get());
			for (CompletableFuture<FileRef> cfFileRef : fileRefs) {
				FileRefNode fileRefNode = fileRefTree.createFileNode(cfFileRef.get());

				node.addFileRefNode(fileRefNode);
			}

			for (Id folderId : node.getFolder().getSubfolders()) {
				Folder folder = getFolderFromId(folderId);
				FileRefNode fileRefNode = fileRefTree.createFileNode(folder);

				node.addFileRefNode(fileRefNode);
				fetchAndAddFileRefsForCourseRecursively(fileRefNode, fileRefTree);
			}
		}

	}

	public Semester getSemesterById(Id id) throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		HttpResponse response;

		try {
			response = httpClient.get(SubPaths.API + Endpoints.SEMESTER.getPath().replace(":semester_id", id.asHex()));
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return null;
		}

		String responseBody = BasicHttpClient.getResponseBody(response);
		JSONObject responseJson = (JSONObject) (new JSONParser().parse(responseBody));

		Semester semester = Semester.fromJson(responseJson);

		return semester;
	}

	public CourseNews getCourseNewsForCourseNewsId(Id courseId, Id courseNewsId) throws NotAuthenticatedException {
		authService.checkIfAuthenticated();

		HttpResponse response;

		try {
			response = httpClient.get(SubPaths.API.toString()
					+ Endpoints.COURSE_NEWS.toString().replace(":news_id", courseNewsId.asHex()));

			if (response.getStatusLine().getStatusCode() / 100 == 2) {
				String responseBody = BasicHttpClient.getResponseBody(response);
				log.debug("getCourseNewsForCourseNewsId: Response: " + responseBody);
				JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);
				responseJson.put("course_id", courseId);

				return CourseNews.fromJson(responseJson);
			} else {
				log.error("Could not get CourseNews for CourseNewsId " + courseNewsId);
				log.error(response.getStatusLine());
				log.error("Returning null instead.");

				return null;
			}

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	public int getAmountCourseNewsForCourseId(Id id) throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		HttpResponse response;

		try {
			response = httpClient.get(SubPaths.API.toString()
					+ Endpoints.ALL_COURSE_NEWS.toString().replace(":course_id", id.asHex()) + "?limit=1");
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return -1;
		}

		if (response.getStatusLine().getStatusCode() / 100 == 2) {
			String responseBody = BasicHttpClient.getResponseBody(response);
			JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

			if (responseJson.containsKey("pagination")) {
				JSONObject paginationJson = (JSONObject) responseJson.get("pagination");

				if (paginationJson.containsKey("total")) {
					return Integer.parseInt(paginationJson.get("total").toString());
				}
			}
		}

		return -1;
	}

	@SuppressWarnings("unchecked")
	public List<CourseNews> getAllCourseNewsForCourseId(Id id) throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		int totalAmountCourseNews = getAmountCourseNewsForCourseId(id);

		int limit = 20;

		List<CourseNews> allCourseNews = new ArrayList<>();

		HttpResponse response;

		for (int offset = 0; offset < totalAmountCourseNews; offset += limit) {
			try {
				response = httpClient.get(
						SubPaths.API.toString() + Endpoints.ALL_COURSE_NEWS.toString().replace(":course_id", id.asHex())
								+ "?offset=" + offset + "&limit=" + limit);
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
				return null;
			}

			if (response.getStatusLine().getStatusCode() / 100 == 2) {
				String responseBody = BasicHttpClient.getResponseBody(response);
				JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

				if (responseJson.containsKey("collection")) {
					((Map<String, JSONObject>) responseJson.get("collection")).forEach((link, courseNewsJson) -> {

						courseNewsJson.put("course_id", id);
						CourseNews courseNews = CourseNews.fromJson(courseNewsJson);

						allCourseNews.add(courseNews);
					});
				}
			}
		}

		return allCourseNews;
	}

}
