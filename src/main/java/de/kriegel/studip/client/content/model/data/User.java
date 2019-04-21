package de.kriegel.studip.client.content.model.data;

import org.json.simple.JSONObject;

import java.io.Serializable;

public class User implements Serializable {

	private final Id id;
	private final String username;
	private final UserInformation userInformation;
	private final String email;
	private final String phone;
	private final String homepage;
	private final String privadr;

	public User(Id id, String username, UserInformation userInformation, String email, String phone, String homepage,
			String privadr) {
		this.id = id;
		this.username = username;
		this.userInformation = userInformation;
		this.email = email;
		this.phone = phone;
		this.homepage = homepage;
		this.privadr = privadr;
	}

	public static User fromJson(JSONObject jsonObject) {
		assert jsonObject != null;
		assert jsonObject.containsKey("user_id");

		Id id = null;
		String username = "";
		UserInformation userInformation = null;
		String email = "";
		String phone = "";
		String homepage = "";
		String privadr = "";
		
		if(jsonObject.containsKey("user_id")) {
			id = new Id(jsonObject.get("user_id").toString());
		} else if(jsonObject.containsKey("id")) {
			// 
			id = new Id(jsonObject.get("id").toString());
		}
		
		if(jsonObject.containsKey("username")) {
			username = jsonObject.get("username").toString();
		}
		
		if(jsonObject.containsKey("name") && jsonObject.get("name") instanceof JSONObject) {
			userInformation = UserInformation.fromJson((JSONObject) jsonObject.get("name"));
			
			// e.g. when fetched from course (REST->json) > lectureres
			if(userInformation != null && username.isEmpty()) {
				username = userInformation.getUsername();
			}
		}
		
		if(jsonObject.containsKey("email")) {
			email = jsonObject.get("email").toString();
		}
		
		if(jsonObject.containsKey("phone")) {
			phone = jsonObject.get("phone").toString();
		}
		
		if(jsonObject.containsKey("homepage")) {
			homepage = jsonObject.get("homepage").toString();
		}
		
		if(jsonObject.containsKey("privadr")) {
			privadr = jsonObject.get("privadr").toString();
		}
		
		return new User(id, username, userInformation, email, phone, homepage, privadr);
	}

	public Id getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public UserInformation getUserInformation() {
		return userInformation;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getHomepage() {
		return homepage;
	}

	public String getPrivadr() {
		return privadr;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof User)) {
			return false;
		}
		User other = (User) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
