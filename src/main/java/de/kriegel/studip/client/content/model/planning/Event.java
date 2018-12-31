package de.kriegel.studip.client.content.model.planning;

import org.json.simple.JSONObject;

import de.kriegel.studip.client.content.model.data.Id;

public class Event {

	private final boolean isCustom;
	private final Id courseId;
	private final long start;
	private final long end;
	private final String content;
	private final String title;
	private final String color;
	private final String type;

	public Event(String eventId, long start, long end, String content, String title, String color, String type) {

		String id;

		// then it might be a custom defined event => no system event like normal
		// Courses!
		if (eventId.length() >= Id.LENGTH && type == null) {
			this.isCustom = true;
			
			this.courseId = null;
		} else {
			this.isCustom = false;

			id = eventId.split("-")[0];
			this.courseId = new Id(id);
		}

		this.start = start;
		this.end = end;
		this.content = content;
		this.title = title;
		this.color = color;
		this.type = type;
	}

	public static Event fromJson(String key, JSONObject jsonObject) {
		assert key != null;
		assert jsonObject != null;

		String eventId = key;
		long start = 0;
		long end = 0;
		String content = "";
		String title = "";
		String color = "";
		String type = "";

		if (jsonObject.containsKey("start")) {
			start = Long.parseLong(jsonObject.get("start").toString());
		}

		if (jsonObject.containsKey("end")) {
			end = Long.parseLong(jsonObject.get("end").toString());
		}

		if (jsonObject.containsKey("content")) {
			content = jsonObject.get("content").toString();
		}

		if (jsonObject.containsKey("title")) {
			title = jsonObject.get("title").toString();
		}

		if (jsonObject.containsKey("color")) {
			color = jsonObject.get("color").toString();
		}

		if (jsonObject.containsKey("type")) {
			type = jsonObject.get("type").toString();
		}

		return new Event(eventId, start, end, content, title, color, type);
	}

	public boolean isCustom() {
		return isCustom;
	}

	public Id getCourseId() {
		if (!isCustom) {
			return courseId;
		} else {
			return null;
		}
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public String getContent() {
		return content;
	}

	public String getTitle() {
		return title;
	}

	public String getColor() {
		return color;
	}

	public String getType() {
		return type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Event)) {
			return false;
		}
		Event other = (Event) obj;
		if (courseId == null) {
			if (other.courseId != null) {
				return false;
			}
		} else if (!courseId.equals(other.courseId)) {
			return false;
		}
		return true;
	}

}
