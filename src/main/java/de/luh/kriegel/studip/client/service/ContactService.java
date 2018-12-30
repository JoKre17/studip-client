package de.luh.kriegel.studip.client.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ContactService {

	private static final Logger log = LogManager.getLogger(ContactService.class);
	
	private final BasicHttpClient httpClient;

	public ContactService(BasicHttpClient httpClient) {
		this.httpClient = httpClient;
	}

}
