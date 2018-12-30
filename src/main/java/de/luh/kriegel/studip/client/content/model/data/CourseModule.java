package de.luh.kriegel.studip.client.content.model.data;

import de.luh.kriegel.studip.client.content.util.RegexHelper;

public class CourseModule {

	private final Id courseId;
	private final CourseModuleType moduleType;
	
	private final String subUrl;
	
	public CourseModule(Id courseId, CourseModuleType moduleType, String subUrl) {
		this.courseId = courseId;
		this.moduleType = moduleType;
		
		this.subUrl = subUrl;
	}

	public static CourseModule fromJson(String moduleKey, String link) {
		assert moduleKey != null;
		assert link != null;
		
		Id courseId = RegexHelper.extractIdFromString(link);
		CourseModuleType moduleType = CourseModuleType.valueOf(moduleKey.toUpperCase());
		
		return new CourseModule(courseId, moduleType, link);
	}

	public Id getCourseId() {
		return courseId;
	}

	public CourseModuleType getModuleType() {
		return moduleType;
	}
	
	public String getSubUrl() {
		return subUrl;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CourseModule)) {
			return false;
		}
		CourseModule other = (CourseModule) obj;
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
