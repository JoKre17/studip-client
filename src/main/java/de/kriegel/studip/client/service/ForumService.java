package de.kriegel.studip.client.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForumService {

	private static final Logger log = LoggerFactory.getLogger(ForumService.class);
	
	private final BasicHttpClient httpClient;

	public ForumService(BasicHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
}
