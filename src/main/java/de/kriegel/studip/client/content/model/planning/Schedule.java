package de.kriegel.studip.client.content.model.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

public class Schedule {

	private final Map<Weekday, List<Event>> eventsPerWeekday;

	public Schedule(Map<Weekday, List<Event>> eventsPerWeekday) {
		this.eventsPerWeekday = eventsPerWeekday;
	}

	@SuppressWarnings("unchecked")
	static Schedule fromJson(JSONObject jsonObject) {
		assert jsonObject != null;

		Map<Weekday, List<Event>> eventsPerWeekday = new HashMap<>();

		((Map<Integer, JSONObject>) jsonObject).forEach((weekdayIndex, coursesJson) -> {
			Weekday weekday = Weekday.fromInt(weekdayIndex);
			List<Event> events = new ArrayList<>();

			if (coursesJson instanceof Map) {
				((Map<String, JSONObject>) coursesJson).forEach((eventId, eventJson) -> {

					Event event = Event.fromJson(eventId, eventJson);
					events.add(event);
				});
			}
			
			eventsPerWeekday.put(weekday, events);

		});

		return new Schedule(eventsPerWeekday);
	}
	
	public Map<Weekday, List<Event>> getEventsPerWeekday() {
		return eventsPerWeekday;
	}
	
	public List<Event> getEventsOfWeekday(Weekday weekday) {
		assert weekday != null;
		
		return eventsPerWeekday.get(weekday);
	}

}
