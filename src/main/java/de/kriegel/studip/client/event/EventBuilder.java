package de.kriegel.studip.client.event;

import org.json.simple.JSONObject;

import de.kriegel.studip.client.exception.NotAuthenticatedException;

public abstract class EventBuilder {
		
	public abstract Event fromJson(JSONObject json) throws NotAuthenticatedException;
		
}
