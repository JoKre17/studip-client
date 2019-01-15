package de.kriegel.studip.client.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kriegel.studip.client.config.Endpoints;
import de.kriegel.studip.client.config.SubPaths;
import de.kriegel.studip.client.content.model.data.Course;
import de.kriegel.studip.client.content.model.data.CourseNews;
import de.kriegel.studip.client.content.model.data.FileRef;
import de.kriegel.studip.client.content.model.data.Folder;
import de.kriegel.studip.client.content.model.data.Id;
import de.kriegel.studip.client.content.model.data.Semester;
import de.kriegel.studip.client.content.model.file.FileRefNode;
import de.kriegel.studip.client.content.model.file.FileRefTree;
import de.kriegel.studip.client.download.DownloadManager;
import de.kriegel.studip.client.exception.NotAuthenticatedException;
import okhttp3.Response;

public class CourseService {

	private static final Logger log = LoggerFactory.getLogger(CourseService.class);

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

		Response response;

		try {
			response = httpClient
					.get(SubPaths.API.toString() + Endpoints.COURSE.toString().replace(":course_id", courseId.asHex()))
					.get();

			if (response.isSuccessful()) {
				String responseBody = httpClient.getResponseBody(response).get();
				JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

				Course course = Course.fromJson(responseJson);

				courseCache.put(courseId, course);

				return course;
			}

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return null;
	}

	public int getAmountCourses() throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		Response response;

		try {
			response = httpClient.get(SubPaths.API.toString()
					+ Endpoints.USER_COURSES.toString().replace(":user_id", authService.getCurrentUserId().asHex())
					+ "?limit=1").get();

			if (response.isSuccessful()) {
				String responseBody = httpClient.getResponseBody(response).get();
				JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

				if (responseJson.containsKey("pagination")) {
					JSONObject paginationJson = (JSONObject) responseJson.get("pagination");

					if (paginationJson.containsKey("total")) {
						return Integer.parseInt(paginationJson.get("total").toString());
					}
				}
			}
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
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

		Response response;

		for (int offset = 0; offset < totalAmountCourses; offset += limit) {
			try {
				response = httpClient.get(SubPaths.API.toString()
						+ Endpoints.USER_COURSES.toString().replace(":user_id", authService.getCurrentUserId().asHex())
						+ "?offset=" + offset + "&limit=" + limit).get();

				if (response.isSuccessful()) {
					String responseBody = httpClient.getResponseBody(response).get();
					JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

					if (responseJson.containsKey("collection")) {
						for (Entry<String, JSONObject> entry : ((Map<String, JSONObject>) responseJson
								.get("collection")).entrySet()) {

							Course course = Course.fromJson(entry.getValue());

							courseCache.putIfAbsent(course.getId(), course);

							allCourses.add(course);
						}
					}
				}

			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
				return null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}

		return allCourses;
	}

	public List<Course> getCoursesForSemesterId(Id semesterId) throws NotAuthenticatedException, ParseException {

		List<Course> allCourses = getAllCourses();

		List<Course> currentCourses = new ArrayList<>();

		for(Course course : allCourses) {
			if(course.getStartSemesterId().equals(semesterId)) {
				currentCourses.add(course);
			}
		}

		return currentCourses;
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

		Response response;
		try {
			response = httpClient
					.get(SubPaths.API.toString()
							+ Endpoints.COURSE_TOP_FOLDER.toString().replace(":course_id", course.getId().asHex()))
					.get();
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return null;
		}

		String responseBody = httpClient.getResponseBody(response).get();
		JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);
		Folder folder = Folder.fromJson(responseJson);

		FileRefTree fileRefTree = new FileRefTree(folder);
		fetchAndAddFileRefsForCourseRecursively(fileRefTree.getRoot(), fileRefTree);

		return fileRefTree;
	}

	private FileRef getFileRefFromId(Id id) throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		Response response;
		try {
			response = httpClient.get(SubPaths.API + Endpoints.FILE.getPath().replace(":file_id", id.asHex())).get();

			String responseBody = httpClient.getResponseBody(response).get();
			JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

			return FileRef.fromJson(responseJson);

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return null;
	}

	private Folder getFolderFromId(Id id) throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		Response response;
		try {
			response = httpClient.get(SubPaths.API + Endpoints.FOLDER.getPath().replace(":folder_id", id.asHex()))
					.get();

			String responseBody = httpClient.getResponseBody(response).get();
			JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

			return Folder.fromJson(responseJson);

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return null;
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

	public int getAmountSemesters() throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		Response response;

		try {
			response = httpClient.get(SubPaths.API.toString() + Endpoints.SEMESTERS.getPath() + "?limit=1").get();

			if (response.isSuccessful()) {
				String responseBody = httpClient.getResponseBody(response).get();
				JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

				if (responseJson.containsKey("pagination")) {
					JSONObject paginationJson = (JSONObject) responseJson.get("pagination");

					if (paginationJson.containsKey("total")) {
						return Integer.parseInt(paginationJson.get("total").toString());
					}
				}
			}
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return -1;
	}

	public List<Semester> getAllSemesters() throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		int totalAmountCourses = getAmountSemesters();

		int limit = 20;

		List<Semester> allSemesters = new ArrayList<>();

		Response response;

		for (int offset = 0; offset < totalAmountCourses; offset += limit) {
			try {
				response = httpClient.get(SubPaths.API.toString() + Endpoints.SEMESTERS.getPath() + "?offset=" + offset
						+ "&limit=" + limit).get();

				if (response.isSuccessful()) {
					String responseBody = httpClient.getResponseBody(response).get();

					JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

					if (responseJson.containsKey("collection")) {

						JSONObject jsonObject = (JSONObject) responseJson.get("collection");

						for (String key : (Set<String>) jsonObject.keySet()) {
							Semester semester = Semester.fromJson((JSONObject) jsonObject.get(key));

							allSemesters.add(semester);
						}
					}
				}

			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
				return null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}

		return allSemesters;
	}

	public Semester getCurrentSemester() throws NotAuthenticatedException, ParseException {

		long now = (long) (new Date().getTime() / 1000.0);

		for (Semester semester : getAllSemesters()) {
			if (semester.getBegin() < now && now < semester.getEnd()) {
				return semester;
			}
		}

		return null;
	}

	public Semester getSemesterById(Id id) throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		Response response;

		try {
			response = httpClient.get(SubPaths.API + Endpoints.SEMESTER.getPath().replace(":semester_id", id.asHex()))
					.get();

			String responseBody = httpClient.getResponseBody(response).get();
			JSONObject responseJson = (JSONObject) (new JSONParser().parse(responseBody));

			Semester semester = Semester.fromJson(responseJson);

			return semester;

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return null;
	}

	public CourseNews getCourseNewsForCourseNewsId(Id courseId, Id courseNewsId) throws NotAuthenticatedException {
		authService.checkIfAuthenticated();

		Response response;

		try {
			response = httpClient.get(SubPaths.API.toString()
					+ Endpoints.COURSE_NEWS.toString().replace(":news_id", courseNewsId.asHex())).get();

			if (response.isSuccessful()) {
				String responseBody = httpClient.getResponseBody(response).get();
				log.debug("getCourseNewsForCourseNewsId: Response: " + responseBody);
				JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);
				responseJson.put("course_id", courseId);

				return CourseNews.fromJson(responseJson);
			} else {
				log.error("Could not get CourseNews for CourseNewsId " + courseNewsId);
				log.error(response.message());
				log.error("Returning null instead.");

				return null;
			}

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return null;
	}

	public int getAmountCourseNewsForCourseId(Id id) throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		Response response;

		try {
			response = httpClient
					.get(SubPaths.API.toString()
							+ Endpoints.ALL_COURSE_NEWS.toString().replace(":course_id", id.asHex()) + "?limit=1")
					.get();

			if (response.isSuccessful()) {
				String responseBody = httpClient.getResponseBody(response).get();
				JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

				if (responseJson.containsKey("pagination")) {
					JSONObject paginationJson = (JSONObject) responseJson.get("pagination");

					if (paginationJson.containsKey("total")) {
						return Integer.parseInt(paginationJson.get("total").toString());
					}
				}
			}

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return -1;
	}

	@SuppressWarnings("unchecked")
	public List<CourseNews> getAllCourseNewsForCourseId(Id id) throws NotAuthenticatedException, ParseException {
		authService.checkIfAuthenticated();

		int totalAmountCourseNews = getAmountCourseNewsForCourseId(id);

		int limit = 20;

		List<CourseNews> allCourseNews = new ArrayList<>();

		Response response;

		for (int offset = 0; offset < totalAmountCourseNews; offset += limit) {
			try {
				response = httpClient.get(
						SubPaths.API.toString() + Endpoints.ALL_COURSE_NEWS.toString().replace(":course_id", id.asHex())
								+ "?offset=" + offset + "&limit=" + limit)
						.get();

				if (response.isSuccessful()) {
					String responseBody = httpClient.getResponseBody(response).get();
					JSONObject responseJson = (JSONObject) new JSONParser().parse(responseBody);

					if (responseJson.containsKey("collection")) {
						for (Entry<String, JSONObject> entry : ((Map<String, JSONObject>) responseJson
								.get("collection")).entrySet()) {
							entry.getValue().put("course_id", id);
							CourseNews courseNews = CourseNews.fromJson(entry.getValue());

							allCourseNews.add(courseNews);
						}
					}
				}

			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
				return null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}

		return allCourseNews;
	}

}
