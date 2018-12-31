package de.kriegel.studip.client.download;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import de.kriegel.studip.client.content.model.data.Course;
import de.kriegel.studip.client.content.model.file.FileRefTree;
import de.kriegel.studip.client.exception.NotAuthenticatedException;
import de.kriegel.studip.client.service.CourseService;
import de.kriegel.studip.client.service.StudIPClient;
import javafx.application.Platform;
import javafx.scene.control.Label;

public class SynchronizeTimer extends Thread {

	private static final Logger log = LogManager.getLogger(SynchronizeTimer.class);

	private StudIPClient studipClient;
	private long sleepTimeMillis = 0;
	private Label nextSynchAtLabel;

	public static final long SLEEP_TIME_BEFORE_FIRST_SYNCH_IN_MILLIS = 30000;
	private boolean firstRun = true;
	private boolean isInitialized = false;

	private CourseService courseService;
	private DownloadManager downloadManager;
	private Map<Course, Course> courseTutorialMap;

	private ExecutorService es;

	private AtomicBoolean cancelled = new AtomicBoolean(false);

	public SynchronizeTimer(StudIPClient studipClient, long sleepTimeMillis, Label nextSynchAtLabel) {
		this.studipClient = studipClient;
		this.nextSynchAtLabel = nextSynchAtLabel;

		this.sleepTimeMillis = sleepTimeMillis;
		this.setDaemon(true);

		try {
			init();
		} catch (NotAuthenticatedException | ParseException e) {
			e.printStackTrace();
		}
	}

	private void init() throws NotAuthenticatedException, ParseException {
		log.debug("Initializing Synchronize Timer");

		courseService = studipClient.getCourseService();
		downloadManager = courseService.getDownloadManager();

		if (courseService != null && downloadManager != null) {
			isInitialized = true;
		}
	}

	private void deinit() {

		if (es != null) {
			es.shutdown();
			while (es.isShutdown()) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	public void updateSleepTimeMillis(long sleepTimeMillis) {

		this.sleepTimeMillis = sleepTimeMillis;
	}

	@Override
	public void run() {

		if (!isInitialized) {
			log.error("Synchronize Timer is not initialized!");
			return;
		}

		es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		// Run
		while (true) {

			setNextSynchAtLabelText();

			// wait 30 seconds before first synch
			if (firstRun) {
				try {
					log.debug("Waiting " + (SLEEP_TIME_BEFORE_FIRST_SYNCH_IN_MILLIS / 1000.0)
							+ " seconds before first run.");
					Thread.sleep(SLEEP_TIME_BEFORE_FIRST_SYNCH_IN_MILLIS);
					firstRun = false;
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
					break;
				}
			}

			try {
				courseTutorialMap = courseService.getCourseTutorialMap();
			} catch (NotAuthenticatedException | ParseException e2) {
				log.error(e2.getMessage(), e2);
				break;
			}

			if (!downloadManager.getDownloadDirectory().exists()
					|| !downloadManager.getDownloadDirectory().isDirectory()) {
				log.info(downloadManager.getDownloadDirectory().getAbsolutePath()
						+ " does not exist or is no directory!");
				break;
			}

			List<CompletableFuture<Void>> downloadTasks = new ArrayList<>();

			log.info("Start Synchronization");

			try {
				for (Entry<Course, Course> e : courseTutorialMap.entrySet()) {

					Course lecture = e.getKey();
					Course tutorial = e.getValue();

					downloadTasks.add(CompletableFuture.runAsync(() -> {
						FileRefTree fileRefTree;

						try {
							fileRefTree = courseService.getFileRefTree(lecture);
							downloadManager.downloadFileRefTree(lecture, fileRefTree, cancelled);

							if (tutorial != null) {
								fileRefTree = courseService.getFileRefTree(tutorial);
								downloadManager.downloadFileRefTree(tutorial, fileRefTree, cancelled);
							}

						} catch (Exception e1) {
							log.error("Inner exception");
							log.error(e1.getMessage(), e1);
						}
					}, es));

				}

				log.info("Start Downloading");
				log.debug("Created " + downloadTasks.size() + " Download tasks.");

				for (int i = 0; i < downloadTasks.size(); i++) {
					if (cancelled.get()) {
						downloadTasks.get(i).cancel(true);
						continue;
					}

					try {
						downloadTasks.get(i).get();
					} catch (InterruptedException | ExecutionException e1) {
						cancelled.set(true);
						log.info("INITIAL interrupt at task " + i);
					}
				}

				log.debug("Sleep Time! for " + (sleepTimeMillis / 60000.0) + " minutes");

				// if run only once at startup, then interrupt here
				if (sleepTimeMillis == 0) {
					Platform.runLater(() -> {
						nextSynchAtLabel.setText("");
					});
					break;
				}

				setNextSynchAtLabelText();
				Thread.sleep(sleepTimeMillis);
			} catch (InterruptedException e) {
				log.warn(e.getMessage(), e);
			}
		}

		deinit();
	}

	private void setNextSynchAtLabelText() {
		Calendar c = Calendar.getInstance();

		if (firstRun) {
			c.add(Calendar.MILLISECOND, (int) SLEEP_TIME_BEFORE_FIRST_SYNCH_IN_MILLIS);
		} else {
			c.add(Calendar.MILLISECOND, (int) sleepTimeMillis);
		}

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		String content = format.format(c.getTime());

		Platform.runLater(() -> {
			nextSynchAtLabel.setText("Next run at " + content + " (local time)");
		});
	}

}
