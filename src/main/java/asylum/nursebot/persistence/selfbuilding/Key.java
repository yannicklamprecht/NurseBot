package asylum.nursebot.persistence.selfbuilding;

public enum Key {
	NONE(""),
	KEY("KEY"),
	UNIQUE("UNIQUE"),
	UNIQUE_KEY("UNIQUE KEY"),
	PRIMARY_KEY("PRIMARY KEY");

	private String stringValue;

	public String toString() {
		return stringValue;
	}

	Key(String stringValue) {
		this.stringValue = stringValue;
	}
}
