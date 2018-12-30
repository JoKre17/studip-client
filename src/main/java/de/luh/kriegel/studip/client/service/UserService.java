package de.luh.kriegel.studip.client.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserService {

	private static final Logger log = LogManager.getLogger(UserService.class);
	
	private final BasicHttpClient httpClient;

	public UserService(BasicHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
}
