package me.mastercapexd.auth;

import java.util.Calendar;

import lombok.NonNull;

public final class TimeUtils {

	public static long parseTime(@NonNull String timeString) {
		Calendar calendar = Calendar.getInstance();
		for (String s : timeString.split(" ")) {
			String lower = s.toLowerCase();
			if (lower.contains("d"))
				calendar.add(Calendar.DATE, Integer.parseInt(lower.replace("d", "")));
			if (lower.contains("h"))
				calendar.add(Calendar.HOUR, Integer.parseInt(lower.replace("h", "")));
			if (lower.contains("m"))
				calendar.add(Calendar.MINUTE, Integer.parseInt(lower.replace("m", "")));
			if (lower.contains("s"))
				calendar.add(Calendar.SECOND, Integer.parseInt(lower.replace("s", "")));
		}
        return calendar.getTimeInMillis() - System.currentTimeMillis();
	}
}