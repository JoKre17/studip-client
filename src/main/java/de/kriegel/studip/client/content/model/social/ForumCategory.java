package de.kriegel.studip.client.content.model.social;

import de.kriegel.studip.client.content.model.data.Id;

public class ForumCategory {

	private final Id id;
	
	public ForumCategory(Id id) {
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
		if (!(obj instanceof ForumCategory)) {
			return false;
		}
		ForumCategory other = (ForumCategory) obj;
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
