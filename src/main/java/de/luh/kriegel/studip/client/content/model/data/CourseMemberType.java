package de.luh.kriegel.studip.client.content.model.data;

public enum CourseMemberType {

	USER("user", "user"), AUTOR("autor", "autor"), TUTOR("tutor", "tutor"), DOZENT("dozent", "dozent");
	
	private String name;
	private String urlQuery;
	
	CourseMemberType(String name, String urlQuery) {
		this.name = name;
		this.urlQuery = "?status=" + urlQuery;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUrlQuery() {
		return urlQuery;
	}
	
}
