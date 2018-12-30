package de.luh.kriegel.studip.client.event;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.luh.kriegel.studip.client.content.model.data.Course;

public class CourseDownloadFinishedEvent extends Event {

	private Course course;
	private List<File> downloadedFiles;

	private final Date eventDate;
	
	public CourseDownloadFinishedEvent(Course course, List<File> downloadedFiles) {
		this.course = course;
		this.downloadedFiles = downloadedFiles;
		
		eventDate = new Date();
	}
	
	public CourseDownloadFinishedEvent(Course course, List<File> downloadedFiles, Date eventDate) {
		this.course = course;
		this.downloadedFiles = downloadedFiles;
		this.eventDate = eventDate;
	}

	public Course getCourse() {
		return course;
	}

	public List<File> getDownloadedFiles() {
		return downloadedFiles;
	}
	
	@Override
	public EventType getEventType() {
		return EventType.DOWNLOAD_COMPLETED;
	}
	
	@Override
	public Date getEventDate() {
		return eventDate;
	}

	@Override
	public String toString() {
		return course.getTitleAsValidFilename() + " Downloaded files: " + downloadedFiles.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((course == null) ? 0 : course.hashCode());
		result = prime * result + ((eventDate == null) ? 0 : eventDate.hashCode());
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
		if (!(obj instanceof CourseDownloadFinishedEvent)) {
			return false;
		}
		CourseDownloadFinishedEvent other = (CourseDownloadFinishedEvent) obj;
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
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		
		json.put("courseId", course.getId().asHex());
		
		JSONArray downloadedFilesJson = new JSONArray();
		for(File file : downloadedFiles) {
			downloadedFilesJson.add(file.getAbsolutePath());
		}
		
		json.put("downloadedFiles", downloadedFilesJson);
		json.put("eventDate", eventDate.getTime());
		
		return json;
	}
	
}
