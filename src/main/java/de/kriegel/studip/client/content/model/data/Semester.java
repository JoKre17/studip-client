package de.kriegel.studip.client.content.model.data;

import org.json.simple.JSONObject;

import de.kriegel.studip.client.content.util.RegexHelper;

import java.io.Serializable;

public class Semester implements Serializable {

	private final Id id;
	private final String title;
	private final String description;
	private final long begin;
	private final long end;
	private final long seminars_begin;
	private final long seminars_end;

	public Semester(Id id, String title, String description, long begin, long end, long seminars_begin,
			long seminars_end) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.begin = begin;
		this.end = end;
		this.seminars_begin = seminars_begin;
		this.seminars_end = seminars_end;
	}

	public static Semester fromJson(JSONObject jsonObject) {
		assert jsonObject != null;
		assert jsonObject.containsKey("id");

		Id id = null;
		String title = "";
		String description = "";
		long begin = 0;
		long end = 0;
		long seminars_begin = 0;
		long seminars_end = 0;

		if (jsonObject.containsKey("id")) {
			id = new Id(jsonObject.get("id").toString());
		}

		if (jsonObject.containsKey("title")) {
			title = jsonObject.get("title").toString();
		}

		if (jsonObject.containsKey("description")) {
			description = jsonObject.get("description").toString();
		}

		if (jsonObject.containsKey("begin")) {
			begin = Long.parseLong(jsonObject.get("begin").toString());
		}

		if (jsonObject.containsKey("end")) {
			end = Long.parseLong(jsonObject.get("end").toString());
		}

		if (jsonObject.containsKey("seminars_begin")) {
			seminars_begin = Long.parseLong(jsonObject.get("seminars_begin").toString());
		}

		if (jsonObject.containsKey("seminars_end")) {
			seminars_end = Long.parseLong(jsonObject.get("seminars_end").toString());
		}

		return new Semester(id, title, description, begin, end, seminars_begin, seminars_end);
	}

	public Id getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getTitleAsValidFilename() {
		return RegexHelper.getValidFilename(title);
	}

	public String getDescription() {
		return description;
	}

	public long getBegin() {
		return begin;
	}

	public long getEnd() {
		return end;
	}

	public long getSeminars_begin() {
		return seminars_begin;
	}

	public long getSeminars_end() {
		return seminars_end;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Semester)) {
			return false;
		}
		Semester other = (Semester) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return title;
	}

}
