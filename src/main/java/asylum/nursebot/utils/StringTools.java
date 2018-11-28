package asylum.nursebot.utils;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.telegram.telegrambots.meta.api.objects.User;


public class StringTools {
	
	public static String makeMention(User user) {
		return user.getUserName() == null ? 
				makeLink(user.getFirstName().replaceAll("_", "\\_"), "tg://user?id=" + user.getId()) : 
				("@" + user.getUserName().replaceAll("_", "\\\\_"));
	}
	
	public static String makeBold(String text) {
		return "*" + text + "*";
	}
	
	public static String makeItalic(String text) {
		return "_" + text + "_";
	}
	
	public static String makeCode(String text) {
		return "`" + text + "`";
	}
	
	public static String makeCodeMultiline(String text) {
		return "```\n" + text + "\n```";
	}
	
	public static String makeLink(String text, String url) {
		return "[" + text + "](" + url + ")";
	}
	
	public static List<String> tokenize(String text) {
		List<String> list = new LinkedList<>();
		
		Matcher m = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(text);
		while (m.find()) {
			if (m.group(1) != null)
				list.add(m.group(1));
			else
				list.add(m.group(2));
		}
		    
		return list;
	}

	public static String getIso8601(Calendar calendar) {
		StringBuilder builder = new StringBuilder();
		builder.append(calendar.get(Calendar.YEAR)).append("-");
		int tmp = calendar.get(Calendar.MONTH) + 1;
		if (tmp < 10)
			builder.append("0");
		builder.append(tmp).append("-");
		tmp = calendar.get(Calendar.DAY_OF_MONTH);
		if (tmp < 10)
			builder.append("0");
		builder.append(tmp).append("T");
		tmp = calendar.get(Calendar.HOUR_OF_DAY);
		if (tmp < 10)
			builder.append("0");
		builder.append(tmp).append(":");
		tmp = calendar.get(Calendar.MINUTE);
		if (tmp < 10)
			builder.append("0");
		builder.append(tmp).append(":");
		tmp = calendar.get(Calendar.SECOND);
		if (tmp < 10)
			builder.append("0");
		builder.append(tmp);
		tmp = calendar.get(Calendar.ZONE_OFFSET);
		tmp += calendar.get(Calendar.DST_OFFSET);
		if (tmp < 0)
			builder.append("-");
		else 
			builder.append("+");
		tmp = Math.abs(tmp);
		tmp /= 1000;
		tmp /= 60;
		int h = tmp / 60;
		int m = tmp % 60;
		if (h < 10)
			builder.append("0");
		builder.append(h);
		if (m != 0) {
			builder.append(":");
			if (m < 10)
				builder.append(":");
			builder.append(m);
		}
		return builder.toString();
	}
}
