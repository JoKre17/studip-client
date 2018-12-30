package de.luh.kriegel.studip.client.service;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.luh.kriegel.studip.client.auth.Credentials;

public class StudIPClient {

	private static final Logger log = LogManager.getLogger(StudIPClient.class);

	private URI baseUri;
	private final BasicHttpClient httpClient;

	private final AuthService authService;
	private final ContactService contactService;
	private final CourseService courseService;
	private final ForumService forumService;
	private final UserService userService;

	public StudIPClient(URI baseUri, Credentials credentials) {
		this.baseUri = baseUri;

		this.httpClient = new BasicHttpClient(baseUri, credentials);

		this.authService = new AuthService(httpClient);
		this.contactService = new ContactService(httpClient);
		this.courseService = new CourseService(httpClient, authService);
		this.forumService = new ForumService(httpClient);
		this.userService = new UserService(httpClient);
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
