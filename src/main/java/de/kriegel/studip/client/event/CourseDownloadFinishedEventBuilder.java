package de.kriegel.studip.client.event;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.kriegel.studip.client.content.model.data.Course;
import de.kriegel.studip.client.content.model.data.Id;
import de.kriegel.studip.client.exception.NotAuthenticatedException;
import de.kriegel.studip.client.service.CourseService;

public class CourseDownloadFinishedEventBuilder extends EventBuilder {

	private final CourseService courseService;

	public CourseDownloadFinishedEventBuilder(CourseService courseService) {
		assert courseService != null;

		this.courseService = courseService;
	};

	@Override
	public CourseDownloadFinishedEvent fromJson(JSONObject json) throws NotAuthenticatedException {

		Id courseId = new Id((String) json.get("courseId"));
		Course course = courseService.getCourseById(courseId);

		JSONArray downloadedFilesJson = (JSONArray) json.get("downloadedFiles");
		List<File> downloadedFiles = new ArrayList<>();
		for (int i = 0; i < downloadedFilesJson.size(); i++) {
			downloadedFiles.add(new File((String) downloadedFilesJson.get(i)));
		}

		Date eventDate = new Date(Long.parseLong(json.get("eventDate").toString()));

		return new CourseDownloadFinishedEvent(course, downloadedFiles, eventDate);
	}

}