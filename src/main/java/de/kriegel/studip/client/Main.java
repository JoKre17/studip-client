package de.kriegel.studip.client;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kriegel.studip.client.auth.Credentials;
import de.kriegel.studip.client.config.Config;
import de.kriegel.studip.client.download.DownloadManager;
import de.kriegel.studip.client.service.AuthService;
import de.kriegel.studip.client.service.CourseService;
import de.kriegel.studip.client.service.StudIPClient;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static Config config;

	public static void main(String[] args) throws Exception {

		config = new Config(args);
		log.info(config.toString());

		if (config.baseUri == null) {
			config.baseUri = new URI("https://studip.uni-hannover.de");
		}

		if (config.credentials == null) {
			config.credentials = new Credentials("JK_14", "Aiedail95");
		}

		StudIPClient studIPClient = new StudIPClient(config.baseUri, config.credentials);

		AuthService authService = studIPClient.getAuthService();
		authService.authenticate();

		CourseService courseService = studIPClient.getCourseService();
		DownloadManager downloadManager = courseService.getDownloadManager();

		log.info(authService.getCurrentUserId().toString());
		
		studIPClient.shutdown();
	}

}
