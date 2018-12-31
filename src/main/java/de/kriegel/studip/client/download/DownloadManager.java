package de.kriegel.studip.client.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import de.kriegel.studip.client.config.Endpoints;
import de.kriegel.studip.client.config.SubPaths;
import de.kriegel.studip.client.content.model.data.Course;
import de.kriegel.studip.client.content.model.data.FileRef;
import de.kriegel.studip.client.content.model.data.Semester;
import de.kriegel.studip.client.content.model.file.FileRefNode;
import de.kriegel.studip.client.content.model.file.FileRefTree;
import de.kriegel.studip.client.event.CourseDownloadFinishedEvent;
import de.kriegel.studip.client.event.CourseDownloadFinishedEventListener;
import de.kriegel.studip.client.event.CourseDownloadProgressEvent;
import de.kriegel.studip.client.exception.NotAuthenticatedException;
import de.kriegel.studip.client.service.BasicHttpClient;
import de.kriegel.studip.client.service.CourseService;

public class DownloadManager extends Observable {

	private static final Logger log = LogManager.getLogger(DownloadManager.class);

	private final List<CourseDownloadFinishedEventListener> courseDownloadFinishedEventListeners = new ArrayList<>();

	private final CourseService courseService;

	private BasicHttpClient httpClient;

	private Path defaultDownloadDirectory;

	private final ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public DownloadManager(CourseService courseService, BasicHttpClient httpClient, Path defaultDownloadDirectory) {
		this.courseService = courseService;
		this.httpClient = httpClient;

		log.info("Init DownloadManager with defaultDownloadDirectory: " + defaultDownloadDirectory.toAbsolutePath());
		this.defaultDownloadDirectory = defaultDownloadDirectory;
	}

	public void close() {
		log.info("Closing DownloadManager");
		es.shutdown();
	}

	public boolean createDirIfNotExists(File dir) {
		if (dir.exists()) {
			if (dir.isDirectory()) {
				return true;
			} else {
				return false;
			}
		} else {
			return dir.mkdirs();
		}
	}

	public File getDownloadDirectory() {
		return defaultDownloadDirectory.toFile();
	}

	public void setDownloadDirectory(File defaultDownloadDirectory) {
		this.defaultDownloadDirectory = defaultDownloadDirectory.toPath();
	}

	public void addCourseDownloadFinishedEventListener(
			CourseDownloadFinishedEventListener courseDownloadFinishedEventListener) {
		this.courseDownloadFinishedEventListeners.add(courseDownloadFinishedEventListener);
	}

	public void removeCourseDownloadFinishedEventListener(
			CourseDownloadFinishedEventListener courseDownloadFinishedEventListener) {
		this.courseDownloadFinishedEventListeners.remove(courseDownloadFinishedEventListener);
	}

	public List<CourseDownloadFinishedEventListener> getCourseDownloadFinishedEventListeners() {
		return courseDownloadFinishedEventListeners;
	}

	private File getSemesterDirectory(Semester semester) {
		return new File(getDownloadDirectory().getAbsolutePath() + "/" + semester.getTitleAsValidFilename());
	}

	private File getCourseDirectory(Course course) {
		Semester semester;
		try {
			semester = courseService.getSemesterById(course.getStartSemesterId());
		} catch (NotAuthenticatedException | ParseException e) {
			e.printStackTrace();
			return null;
		}

		String courseFilename = course.getTitleAsValidFilename();

		if (course.isTutorium()) {
			courseFilename = courseFilename.replace("Übung", "").trim() + "/Übung";
		} else {
			courseFilename = courseFilename + "/Vorlesung";
		}

		return new File(getSemesterDirectory(semester).getAbsolutePath() + "/" + courseFilename);
	}

	public boolean createDownloadDirectoryIfNotExists() {
		File downloadDir = getDownloadDirectory();

		return createDirIfNotExists(downloadDir);
	}

	public boolean createSemesterDirectoryIfNotExists(Semester semester) {
		File semesterDir = getSemesterDirectory(semester);

		return createDirIfNotExists(semesterDir);
	}

	public boolean createCourseDirectoryIfNotExists(Course course) {
		File courseDir = getCourseDirectory(course);

		return createDirIfNotExists(courseDir);
	}

	public void downloadFileRefTree(Course course, FileRefTree fileRefTree, AtomicBoolean cancelled) {

		if (!createCourseDirectoryIfNotExists(course)) {
			return;
		}

		File parentDir = getCourseDirectory(course);

		Queue<CompletableFuture<Void>> downloadTasks = new ConcurrentLinkedQueue<>();
		List<File> toBeDownloadedFiles = new ArrayList<>();

		downloadFileRefTreeRecursive(parentDir, fileRefTree.getRoot(), downloadTasks, toBeDownloadedFiles);

		int size = downloadTasks.size();

		int count = 0;
		log.debug(count + "/" + size + " : " + course.getTitle());
		for (CompletableFuture<Void> task : downloadTasks) {
			if (cancelled != null && cancelled.get()) {
				task.cancel(true);
				continue;
			}

			try {
				task.get();
				count++;
				log.debug(count + "/" + size + " : " + course.getTitle());
				setChanged();
				notifyObservers(new CourseDownloadProgressEvent(course, count / (double) size));
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

		}

		if (cancelled != null && cancelled.get()) {
			return;
		}

		if (size > 0) {
			setChanged();
			notifyObservers(new CourseDownloadProgressEvent(course, count / (double) size));
			for (CourseDownloadFinishedEventListener eventListener : courseDownloadFinishedEventListeners) {
				eventListener.onCourseDownloadFinished(new CourseDownloadFinishedEvent(course, toBeDownloadedFiles));
			}
			log.info("DONE : " + course.getTitle());
		}

	}

	private void downloadFileRefTreeRecursive(File parentDir, FileRefNode node,
			Queue<CompletableFuture<Void>> downloadTasks, List<File> toBeDownloadedFiles) {

		for (FileRefNode child : node.getChildren()) {
			if (child.isDirectory()) {
				File dir = new File(parentDir.getAbsolutePath() + "/" + child.getFolder().getNameValidAsFilename());
				createDirIfNotExists(dir);

				downloadFileRefTreeRecursive(dir, child, downloadTasks, toBeDownloadedFiles);
			} else {

				FileRef fileRef = child.getFileRef();
				File outputFile = new File(parentDir.getAbsolutePath() + "/" + fileRef.getName());

				// dont create a download task for a file which has already been downloaded AND
				// is up to date
				if (outputFile.exists()) {
					if (fileRef.getChdate() < outputFile.lastModified()) {
						continue;
					}
				} else {
					try {
						outputFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				toBeDownloadedFiles.add(outputFile);

				downloadTasks.add(CompletableFuture.runAsync(() -> {

					HttpResponse response;

					try {
						response = httpClient.get(SubPaths.API
								+ Endpoints.FILE_DOWNLOAD.getPath().replace(":file_id", fileRef.getId().asHex()));

						BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));

						byte[] buffer = new byte[512];

						while (bis.read(buffer) != -1) {
							bos.write(buffer);
						}

						bis.close();
						bos.close();

						log.debug("Downloaded " + child.getFileRef().getName() + " " + (outputFile.length() / 1048576f)
								+ " MB");

					} catch (URISyntaxException | IOException e) {
						e.printStackTrace();
					}
				}, es));
			}
		}
	}

}
