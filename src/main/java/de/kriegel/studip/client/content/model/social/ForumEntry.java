package de.kriegel.studip.client.content.model.social;

import de.kriegel.studip.client.content.model.data.Id;

public class ForumEntry {

	private final Id id;

	public ForumEntry(Id id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ForumEntry)) {
			return false;
		}
		ForumEntry other = (ForumEntry) obj;
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
