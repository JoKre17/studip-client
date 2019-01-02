package de.kriegel.studip.client.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactService {

	private static final Logger log = LoggerFactory.getLogger(ContactService.class);
	
	private final BasicHttpClient httpClient;

	public ContactService(BasicHttpClient httpClient) {
		this.httpClient = httpClient;
	}

}
