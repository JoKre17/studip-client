package de.luh.kriegel.studip.client.content.model.data;

public enum CourseModuleType {

	FORUM("forum", "forum_categories"), DOCUMENTS("documents", "files"), WIKI("wiki", "wiki");
	
	private String key;
	private String subpath;
	
	CourseModuleType(String key, String subpath) {
		this.key = key;
		this.subpath = subpath;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getSubpath() {
		return subpath;
	}
}
