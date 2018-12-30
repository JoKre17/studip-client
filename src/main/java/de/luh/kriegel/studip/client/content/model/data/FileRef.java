package de.luh.kriegel.studip.client.content.model.data;

import org.json.simple.JSONObject;

public class FileRef {

	private final Id id;
	private final Id file_id;
	private final Id folder_id;
	private final int downloads;
	private final String description;
	private final String content_terms_of_use_id;
	private final Id user_id;
	private final String name;
	private final long mkdate;
	private final long chdate;
	private final boolean is_readable;
	private final boolean is_downloadable;
	private final boolean is_editable;
	private final boolean is_writable;
	private final int size;
	private final String mime_type;
	private final String storage;

	public FileRef(Id id, Id file_id, Id folder_id, int downloads, String description, String content_terms_of_use_id,
			Id user_id, String name, long mkdate, long chdate, boolean is_readable, boolean is_downloadable,
			boolean is_editable, boolean is_writable, int size, String mime_type, String storage) {
		this.id = id;
		this.file_id = file_id;
		this.folder_id = folder_id;
		this.downloads = downloads;
		this.description = description;
		this.content_terms_of_use_id = content_terms_of_use_id;
		this.user_id = user_id;
		this.name = name;
		this.mkdate = mkdate;
		this.chdate = chdate;
		this.is_readable = is_readable;
		this.is_downloadable = is_downloadable;
		this.is_editable = is_editable;
		this.is_writable = is_writable;
		this.size = size;
		this.mime_type = mime_type;
		this.storage = storage;
	}

	public static FileRef fromJson(JSONObject jsonObject) {
		assert jsonObject != null;
		assert jsonObject.containsKey("id");

		Id id = null;
		Id file_id = null;
		Id folder_id = null;
		int downloads = 0;
		String description = "";
		String content_terms_of_use_id = "";
		Id user_id = null;
		String name = "";
		long mkdate = 0;
		long chdate = 0;
		boolean is_readable = false;
		boolean is_downloadable = false;
		boolean is_editable = false;
		boolean is_writable = false;
		int size = 0;
		String mime_type = "";
		String storage = "";

		if (jsonObject.containsKey("id")) {
			id = new Id(jsonObject.get("id").toString());
		}

		if (jsonObject.containsKey("file_id")) {
			file_id = new Id(jsonObject.get("file_id").toString());
		}

		if (jsonObject.containsKey("folder_id")) {
			folder_id = new Id(jsonObject.get("folder_id").toString());
		}

		if (jsonObject.containsKey("downloads")) {
			downloads = Integer.parseInt(jsonObject.get("downloads").toString());
		}

		if (jsonObject.containsKey("description")) {
			description = jsonObject.get("description").toString();
		}

		if (jsonObject.containsKey("content_terms_of_use_id")) {
			content_terms_of_use_id = jsonObject.get("content_terms_of_use_id").toString();
		}

		if (jsonObject.containsKey("user_id")) {
			user_id = new Id(jsonObject.get("user_id").toString());
		}

		if (jsonObject.containsKey("name")) {
			name = jsonObject.get("name").toString();
		}

		if (jsonObject.containsKey("mkdate")) {
			mkdate = Long.parseLong(jsonObject.get("mkdate").toString());
		}

		if (jsonObject.containsKey("chdate")) {
			chdate = Long.parseLong(jsonObject.get("chdate").toString());
		}

		if (jsonObject.containsKey("is_readable")) {
			is_readable = Boolean.parseBoolean(jsonObject.get("is_readable").toString());
		}

		if (jsonObject.containsKey("is_downloadable")) {
			is_downloadable = Boolean.parseBoolean(jsonObject.get("is_downloadable").toString());
		}

		if (jsonObject.containsKey("is_editable")) {
			is_editable = Boolean.parseBoolean(jsonObject.get("is_editable").toString());
		}

		if (jsonObject.containsKey("is_writable")) {
			is_writable = Boolean.parseBoolean(jsonObject.get("is_writable").toString());
		}

		if (jsonObject.containsKey("size")) {
			size = Integer.parseInt(jsonObject.get("size").toString());
		}

		if (jsonObject.containsKey("mime_type")) {
			mime_type = jsonObject.get("mime_type").toString();
		}

		if (jsonObject.containsKey("storage")) {
			storage = jsonObject.get("storage").toString();
		}

		return new FileRef(id, file_id, folder_id, downloads, description, content_terms_of_use_id, user_id, name,
				mkdate, chdate, is_readable, is_downloadable, is_editable, is_writable, size, mime_type, storage);
	}
	
	public Id getId() {
		return id;
	}

	public Id getFile_id() {
		return file_id;
	}

	public Id getFolder_id() {
		return folder_id;
	}

	public int getDownloads() {
		return downloads;
	}

	public String getDescription() {
		return description;
	}

	public String getContent_terms_of_use_id() {
		return content_terms_of_use_id;
	}

	public Id getUser_id() {
		return user_id;
	}

	public String getName() {
		return name;
	}

	public long getMkdate() {
		return mkdate;
	}

	public long getChdate() {
		return chdate;
	}

	public boolean isIs_readable() {
		return is_readable;
	}

	public boolean isIs_downloadable() {
		return is_downloadable;
	}

	public boolean isIs_editable() {
		return is_editable;
	}

	public boolean isIs_writable() {
		return is_writable;
	}

	public int getSize() {
		return size;
	}

	public String getMime_type() {
		return mime_type;
	}

	public String getStorage() {
		return storage;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof FileRef)) {
			return false;
		}
		FileRef other = (FileRef) obj;
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
