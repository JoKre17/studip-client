package de.kriegel.studip.client.download;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.parser.ParseException;

import de.kriegel.studip.client.content.model.data.Course;
import de.kriegel.studip.client.content.model.file.FileRefTree;
import de.kriegel.studip.client.exception.NotAuthenticatedException;
import de.kriegel.studip.client.service.CourseService;
import de.kriegel.studip.client.service.StudIPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchronizeTimer extends Thread {

	private static final Logger log = LoggerFactory.getLogger(SynchronizeTimer.class);

	private List<SynchronizeTimerTriggeredListener> synchronizeTimerTriggeredListeners;
	
	private StudIPClient studipClient;
	private long sleepTimeMillis = 0;

	public static final long SLEEP_TIME_BEFORE_FIRST_SYNCH_IN_MILLIS = 30000;
	private boolean firstRun = true;
	private boolean isInitialized = false;

	private CourseService courseService;
	private DownloadManager downloadManager;
	private Map<Course, Course> courseTutorialMap;

	private ExecutorService es;

	private AtomicBoolean cancelled = new AtomicBoolean(false);

	public SynchronizeTimer(StudIPClient studipClient, long sleepTimeMillis) {
		this.studipClient = studipClient;

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

			// wait 30 seconds before first synch
			if (firstRun) {
				
				Calendar c = Calendar.getInstance();
				c.add(Calendar.MILLISECOND, (int) SLEEP_TIME_BEFORE_FIRST_SYNCH_IN_MILLIS);
				synchronizeTimerTriggeredListeners.forEach(trigger -> trigger.onTrigger(c.getTime()));
				
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
					synchronizeTimerTriggeredListeners.forEach(trigger -> trigger.onTrigger(new Date(0)));
					break;
				}
				
				Calendar c = Calendar.getInstance();
				c.add(Calendar.MILLISECOND, (int) sleepTimeMillis);
				synchronizeTimerTriggeredListeners.forEach(trigger -> trigger.onTrigger(c.getTime()));

				Thread.sleep(sleepTimeMillis);
			} catch (InterruptedException e) {
				log.warn(e.getMessage(), e);
			}
		}

		deinit();
	}

	public void addSynchronizeTimerTriggeredListener(SynchronizeTimerTriggeredListener listener) {
		this.synchronizeTimerTriggeredListeners.add(listener);
	}
	
	public void removeSynchronizeTimerTriggeredListener(SynchronizeTimerTriggeredListener listener) {
		this.synchronizeTimerTriggeredListeners.remove(listener);
	}
	
	public interface SynchronizeTimerTriggeredListener {
		
		public void onTrigger(Date nextTriggerDate);
		
	}

}
