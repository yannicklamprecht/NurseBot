package asylum.nursebot.exceptions;

public class WhatTheFuckException extends NurseException {

	public WhatTheFuckException(String string) {
		super("WhatTheFuckException message: " + string);
	}

}
