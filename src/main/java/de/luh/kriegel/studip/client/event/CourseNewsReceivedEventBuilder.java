package de.luh.kriegel.studip.client.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import de.luh.kriegel.studip.client.content.model.data.CourseNews;
import de.luh.kriegel.studip.client.content.model.data.Id;
import de.luh.kriegel.studip.client.exception.NotAuthenticatedException;
import de.luh.kriegel.studip.client.service.CourseService;

public class CourseNewsReceivedEventBuilder extends EventBuilder {

	private static final Logger log = LogManager.getLogger(CourseNewsReceivedEventBuilder.class);

	private CourseService courseService;

	public CourseNewsReceivedEventBuilder(CourseService courseService) {
		this.courseService = courseService;
	}

	@Override
	public CourseNewsReceivedEvent fromJson(JSONObject json) throws NotAuthenticatedException {

		Id courseId = new Id(json.get("courseId").toString());
		Id courseNewsId = new Id(json.get("courseNewsId").toString());

		log.debug("Build CourseNewsReceivedEvent fromJson " + "id: " + courseNewsId);

		CourseNews courseNews = courseService.getCourseNewsForCourseNewsId(courseId, courseNewsId);

		return new CourseNewsReceivedEvent(courseNews);
	}

}