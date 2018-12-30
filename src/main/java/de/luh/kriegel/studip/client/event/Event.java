package de.luh.kriegel.studip.client.event;

import java.util.Date;

import org.json.simple.JSONObject;

public abstract class Event {

	public abstract EventType getEventType();
	
	public abstract Date getEventDate();
	
	public abstract JSONObject toJson();
	
	@SuppressWarnings("unchecked")
	public JSONObject toJsonIdentifier() {
		JSONObject json = new JSONObject();
		
		json.put("eventType", getEventType().name());
		json.put("date", getEventDate().getTime());
		
		return json;
	}
	
	@Override
	public abstract boolean equals(Object obj);
	
}

