package de.kriegel.studip.client.service;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kriegel.studip.client.auth.Credentials;

public class StudIPClient {

	private static final Logger log = LoggerFactory.getLogger(StudIPClient.class);

	private URI baseUri;
	private final BasicHttpClient httpClient;

	private final AuthService authService;
	private final ContactService contactService;
	private final CourseService courseService;
	private final ForumService forumService;
	private final UserService userService;

	private final ExecutorService executorService;

	public StudIPClient(URI baseUri, Credentials credentials) {
		this.baseUri = baseUri;
		executorService =  new ThreadPoolExecutor(4,4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		this.httpClient = new BasicHttpClient(baseUri, credentials, executorService);

		this.authService = new AuthService(httpClient);
		this.contactService = new ContactService(httpClient);
		this.courseService = new CourseService(httpClient, authService);
		this.forumService = new ForumService(httpClient);
		this.userService = new UserService(httpClient);
	}
	
	public void shutdown() {
		log.info("Shutting down StudIPClient");
		executorService.shutdown();
		log.info("Shut down executor service");
	}
	
	public AuthService getAuthService() {
		return authService;
	}

	public ContactService getContactService() {
		return contactService;
	}
	
	public CourseService getCourseService() {
		return courseService;
	}
	
	public ForumService getForumService() {
		return forumService;
	}
	
	public UserService getUserService() {
		return userService;
	}

}
