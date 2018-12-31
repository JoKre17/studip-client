package de.kriegel.studip.client.content.model.planning;

public enum Weekday implements Comparable<Weekday>{

	MONDAY("Monday", "Montag"), TUESDAY("Tuesday", "Dienstag"), WENDNESDAY("Wednesday", "Mittwoch"), THURSDAY(
			"Thursday",
			"Donnerstag"), FRIDAY("Friday", "Freitag"), SATURDAY("Saturday", "Samstag"), SUNDAY("Sunday", "Sonntag");

	private String name_en;
	private String name_de;

	private Weekday(String name_en, String name_de) {
		this.name_en = name_en;
		this.name_de = name_de;
	}

	public String toReadableEnglish() {
		return this.name_en;
	}

	public String toReadableGerman() {
		return this.name_de;
	}
	
	public static Weekday fromInt(int index) {
		if(index >= 0 && index < Weekday.values().length) {
			return Weekday.values()[index];
		} else {
			return null;
		}
	}

}
