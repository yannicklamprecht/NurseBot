package asylum.nursebot.utils;

public class ExceptionIgnorer {
	
	public static void ignore(Class<? extends Exception> clazz, ToBeIgnored action) {
		ignore(clazz, true, action);
	}
	
	public static void ignore(Class<? extends Exception> clazz, boolean stackTrace, ToBeIgnored action) {
		try {
			action.action();
		} catch(Exception e) {
			if (clazz.isInstance(e)) {
				if (stackTrace)
					e.printStackTrace();
			} else {
				throw new RuntimeException(e);
			}
		}
	}
}
