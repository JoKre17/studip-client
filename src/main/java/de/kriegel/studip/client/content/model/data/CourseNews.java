package de.kriegel.studip.client.content.model.data;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CourseNews {

	private final Id id;
	private final String topic;
	private final String body;
	private final long date;
	private final Id user_id;
	private final long expire;
	private final int allow_comments;
	private final long chdate;
	private final Id chdate_uid;
	private final long mkdate;
	private final String body_html;
	private final List<String> ranges;

	private final Id courseId;

	public CourseNews(Id courseId, Id id, String topic, String body, long date, Id user_id, long expire,
			int allow_comments, long chdate, Id chdate_uid, long mkdate, String body_html, List<String> ranges) {
		super();
		this.courseId = courseId;
		this.id = id;
		this.topic = topic;
		this.body = body;
		this.date = date;
		this.user_id = user_id;
		this.expire = expire;
		this.allow_comments = allow_comments;
		this.chdate = chdate;
		this.chdate_uid = chdate_uid;
		this.mkdate = mkdate;
		this.body_html = body_html;
		this.ranges = ranges;
	}

	@SuppressWarnings("unchecked")
	public static CourseNews fromJson(JSONObject jsonObject) {
		assert jsonObject != null;
		assert jsonObject.containsKey("news_id");

		Id courseId = null;

		Id id = null;
		String topic = "";
		String body = "";
		long date = 0;
		Id user_id = null;
		long expire = 0;
		int allow_comments = 0;
		long chdate = 0;
		Id chdate_uid = null;
		long mkdate = 0;
		String body_html = "";
		List<String> ranges = new ArrayList<>();

		if (jsonObject.containsKey("course_id")) {
			courseId = new Id(jsonObject.get("course_id").toString());
		}

		if (jsonObject.containsKey("news_id")) {
			id = new Id(jsonObject.get("news_id").toString());
		}

		if (jsonObject.containsKey("topic")) {
			topic = jsonObject.get("topic").toString();
		}

		if (jsonObject.containsKey("body")) {
			body = jsonObject.get("body").toString();
		}

		if (jsonObject.containsKey("date")) {
			date = Long.parseLong(jsonObject.get("date").toString()) * 1000;
		}

		if (jsonObject.containsKey("user_id")) {
			user_id = new Id(jsonObject.get("user_id").toString());
		}

		if (jsonObject.containsKey("expire")) {
			expire = Long.parseLong(jsonObject.get("expire").toString());
		}

		if (jsonObject.containsKey("allow_comments")) {
			allow_comments = Integer.parseInt(jsonObject.get("allow_comments").toString());
		}

		if (jsonObject.containsKey("chdate")) {
			chdate = Long.parseLong(jsonObject.get("chdate").toString()) * 1000;
		}

		if (jsonObject.containsKey("chdate_uid")) {
			if (!jsonObject.get("chdate_uid").equals("")) {
				chdate_uid = new Id(jsonObject.get("chdate_uid").toString());
			}
		}

		if (jsonObject.containsKey("mkdate")) {
			mkdate = Long.parseLong(jsonObject.get("mkdate").toString()) * 1000;
		}

		if (jsonObject.containsKey("body_html")) {
			body_html = jsonObject.get("body_html").toString();
		}

		if (jsonObject.containsKey("ranges")) {

			JSONArray rangesJson = (JSONArray) jsonObject.get("ranges");
			for(int i = 0; i < rangesJson.size(); i++) {
				ranges.add(rangesJson.get(i).toString());
			}
		}

		return new CourseNews(courseId, id, topic, body, date, user_id, expire, allow_comments, chdate, chdate_uid,
				mkdate, body_html, ranges);
	}

	public Id getCourseId() {
		return courseId;
	}

	public Id getId() {
		return id;
	}

	public String getTopic() {
		return topic;
	}

	public String getBody() {
		return body;
	}

	public long getDate() {
		return date;
	}

	public Id getUser_id() {
		return user_id;
	}

	public long getExpire() {
		return expire;
	}

	public int getAllow_comments() {
		return allow_comments;
	}

	public long getChdate() {
		return chdate;
	}

	public Id getChdate_uid() {
		return chdate_uid;
	}

	public long getMkdate() {
		return mkdate;
	}

	public String getBody_html() {
		return body_html;
	}

	public List<String> getRanges() {
		return ranges;
	}

	@Override
	public String toString() {
		return id.toString() + " - " + getTopic();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + allow_comments;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((body_html == null) ? 0 : body_html.hashCode());
		result = prime * result + (int) (chdate ^ (chdate >>> 32));
		result = prime * result + ((chdate_uid == null) ? 0 : chdate_uid.hashCode());
		result = prime * result + (int) (date ^ (date >>> 32));
		result = prime * result + (int) (expire ^ (expire >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (int) (mkdate ^ (mkdate >>> 32));
		result = prime * result + ((ranges == null) ? 0 : ranges.hashCode());
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		result = prime * result + ((user_id == null) ? 0 : user_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CourseNews)) {
			return false;
		}
		CourseNews other = (CourseNews) obj;
		if (allow_comments != other.allow_comments) {
			return false;
		}
		if (body == null) {
			if (other.body != null) {
				return false;
			}
		} else if (!body.equals(other.body)) {
			return false;
		}
		if (body_html == null) {
			if (other.body_html != null) {
				return false;
			}
		} else if (!body_html.equals(other.body_html)) {
			return false;
		}
		if (chdate != other.chdate) {
			return false;
		}
		if (chdate_uid == null) {
			if (other.chdate_uid != null) {
				return false;
			}
		} else if (!chdate_uid.equals(other.chdate_uid)) {
			return false;
		}
		if (date != other.date) {
			return false;
		}
		if (expire != other.expire) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (courseId == null) {
			if (other.courseId != null) {
				return false;
			}
		} else if (!courseId.equals(other.courseId)) {
			return false;
		}
		if (mkdate != other.mkdate) {
			return false;
		}
		if (ranges == null) {
			if (other.ranges != null) {
				return false;
			}
		} else if (!ranges.equals(other.ranges)) {
			return false;
		}
		if (topic == null) {
			if (other.topic != null) {
				return false;
			}
		} else if (!topic.equals(other.topic)) {
			return false;
		}
		if (user_id == null) {
			if (other.user_id != null) {
				return false;
			}
		} else if (!user_id.equals(other.user_id)) {
			return false;
		}
		return true;
	}

}
