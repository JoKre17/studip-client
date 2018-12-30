package de.luh.kriegel.studip.client.config;

public enum Endpoints {

	MESSAGES("/messages/write"), SEMESTER("/semester/:semester_id"), COURSE("/course/:course_id"), COURSE_NEWS("/news/:news_id"), ALL_COURSE_NEWS(
			"/course/:course_id/news"), COURSE_TOP_FOLDER("/course/:course_id/top_folder"), FILE(
					"/file/:file_id"), FILE_DOWNLOAD("/file/:file_id/download"), FOLDER(
							"/folder/:folder_id"), USER("/user"), USER_COURSES("/user/:user_id/courses");

	private final String path;

	private Endpoints(String path) {
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	@Override
	public String toString() {
		return path;
	}

}
