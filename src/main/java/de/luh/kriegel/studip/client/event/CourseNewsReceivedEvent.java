package de.luh.kriegel.studip.client.event;

import java.util.Date;

import org.json.simple.JSONObject;

import de.luh.kriegel.studip.client.content.model.data.CourseNews;

public class CourseNewsReceivedEvent extends Event {

	private final CourseNews courseNews;

	public CourseNewsReceivedEvent(CourseNews courseNews) {
		this.courseNews = courseNews;
	}

	public CourseNews get() {
		return courseNews;
	}

	@Override
	public EventType getEventType() {
		return EventType.COURSE_NEWS_RECEIVED;
	}

	@Override
	public Date getEventDate() {
		return new Date(courseNews.getMkdate());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((courseNews == null) ? 0 : courseNews.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CourseNewsReceivedEvent)) {
			return false;
		}
		CourseNewsReceivedEvent other = (CourseNewsReceivedEvent) obj;
		if (courseNews == null) {
			if (other.courseNews != null) {
				return false;
			}
		} else if (!courseNews.equals(other.courseNews)) {
			return false;
		}
		return true;
	}

	@Override
	public JSONObject toJson() {

		JSONObject json = new JSONObject();

		json.put("courseId", courseNews.getCourseId().asHex());
		json.put("courseNewsId", courseNews.getId().asHex());

		return json;
	}

}
