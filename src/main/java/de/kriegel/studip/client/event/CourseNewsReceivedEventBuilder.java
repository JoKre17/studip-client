package de.kriegel.studip.client.event;

import org.json.simple.JSONObject;

import de.kriegel.studip.client.content.model.data.CourseNews;
import de.kriegel.studip.client.content.model.data.Id;
import de.kriegel.studip.client.exception.NotAuthenticatedException;
import de.kriegel.studip.client.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CourseNewsReceivedEventBuilder extends EventBuilder {

    private static final Logger log = LoggerFactory.getLogger(CourseNewsReceivedEventBuilder.class);

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