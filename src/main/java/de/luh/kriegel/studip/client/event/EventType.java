package de.luh.kriegel.studip.client.event;

public enum EventType {
	DOWNLOAD_COMPLETED("Download"), COURSE_DOWNLOAD_PROGRESS("Download Progress"), COURSE_NEWS_RECEIVED("Course News"), DEFAULT("None");

	private final String simpleName;
	
	EventType(String simpleName) {
		this.simpleName = simpleName;
	}
	
	public EventType getEventTypeForEvent(Object o) {

		if (o instanceof CourseDownloadFinishedEvent) {
			return DOWNLOAD_COMPLETED;
		}

		if (o instanceof CourseNewsReceivedEvent) {
			return COURSE_NEWS_RECEIVED;
		}
		
		if(o instanceof CourseDownloadProgressEvent) {
			return COURSE_DOWNLOAD_PROGRESS;
		}

		return DEFAULT;
	}
	
	public String getSimpleName() {
		return simpleName;
	}
}
