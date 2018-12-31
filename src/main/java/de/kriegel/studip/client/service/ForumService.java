package de.kriegel.studip.client.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForumService {

	private static final Logger log = LogManager.getLogger(ForumService.class);
	
	private final BasicHttpClient httpClient;

	public ForumService(BasicHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
}
