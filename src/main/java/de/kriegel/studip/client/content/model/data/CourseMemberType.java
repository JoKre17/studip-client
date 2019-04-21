package de.kriegel.studip.client.content.model.data;


import java.io.Serializable;

public enum CourseMemberType implements Serializable {

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
