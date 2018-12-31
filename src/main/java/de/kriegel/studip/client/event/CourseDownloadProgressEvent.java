package de.kriegel.studip.client.event;

import java.util.Date;

import org.json.simple.JSONObject;

import de.kriegel.studip.client.content.model.data.Course;

public class CourseDownloadProgressEvent extends Event {

	private Course course;
	private double progress;
	
	private final Date eventDate;
	
	public CourseDownloadProgressEvent(Course course, double progress) {
		this.course = course;
		this.progress = progress;

		this.eventDate = new Date();
	}

	public CourseDownloadProgressEvent(Course course, double progress, Date eventDate) {
		this.course = course;
		this.progress = progress;
		this.eventDate = eventDate;
	}

	public Course getCourse() {
		return course;
	}

	public double getProgress() {
		return progress;
	}
	
	public double getProgressInPercent() {
		return Math.round(progress * 100.0) / 100.0;
	}
	
	@Override
	public EventType getEventType() {
		return EventType.COURSE_DOWNLOAD_PROGRESS;
	}
	
	@Override
	public Date getEventDate() {
		return eventDate;
	}

	@Override
	public String toString() {
		return course.getTitleAsValidFilename() + " Progress: " + Math.round(progress * 100.0) / 100.0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((course == null) ? 0 : course.hashCode());
		result = prime * result + ((eventDate == null) ? 0 : eventDate.hashCode());
		long temp;
		temp = Double.doubleToLongBits(progress);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (!(obj instanceof CourseDownloadProgressEvent)) {
			return false;
		}
		CourseDownloadProgressEvent other = (CourseDownloadProgressEvent) obj;
		if (course == null) {
			if (other.course != null) {
				return false;
			}
		} else if (!course.equals(other.course)) {
			return false;
		}
		if (eventDate == null) {
			if (other.eventDate != null) {
				return false;
			}
		} else if (!eventDate.equals(other.eventDate)) {
			return false;
		}
		if (Double.doubleToLongBits(progress) != Double.doubleToLongBits(other.progress)) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		
		json.put("courseId", course.getId().asHex());
		json.put("progress", progress);
		json.put("eventDate", eventDate.getTime());
		
		return json;
	}
	
}
