package de.luh.kriegel.studip.client.content.model.data;

import org.json.simple.JSONObject;

public class UserInformation {

	private final String username;
	private final String formatted;
	private final String family;
	private final String given;
	private final String prefix;
	private final String suffix;
	
	/**
	 * Constructor containing all variables, but some may be null
	 * @param username
	 * @param formatted
	 * @param family
	 * @param given
	 * @param prefix
	 * @param suffix
	 */
	public UserInformation(String username, String formatted, String family, String given, String prefix, String suffix) {

		this.username = username;
		this.formatted = formatted;
		this.family = family;
		this.given = given;
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public static UserInformation fromJson(JSONObject jsonObject) {
		assert jsonObject != null;
		
		String username = "";
		String formatted = "";
		String family ="";
		String given = "";
		String prefix = "";
		String suffix = "";
		
		if(jsonObject.containsKey("username")) {
			username = jsonObject.get("username").toString();
		}
		
		if(jsonObject.containsKey("formatted")) {
			formatted = jsonObject.get("formatted").toString();
		}
		
		if(jsonObject.containsKey("family")) {
			family = jsonObject.get("family").toString();
		}
		
		if(jsonObject.containsKey("given")) {
			given = jsonObject.get("given").toString();
		}
		
		if(jsonObject.containsKey("prefix")) {
			prefix = jsonObject.get("prefix").toString();
		}

		if(jsonObject.containsKey("suffix")) {
			suffix = jsonObject.get("suffix").toString();
		}
		
		return new UserInformation(username, formatted, family, given,  prefix, suffix);
		
	}

	public String getUsername() {
		return username;
	}

	public String getFormatted() {
		return formatted;
	}

	public String getFamily() {
		return family;
	}

	public String getGiven() {
		return given;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Course)) {
			return false;
		}
		UserInformation other = (UserInformation) obj;
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}
	
}
