package de.kriegel.studip.client.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	
	private final BasicHttpClient httpClient;

	public UserService(BasicHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
}
