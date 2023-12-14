package sprest.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 */
public abstract class DateUtils {

    public static long DAY = 86400000L;

	public static Date convert(LocalDate date) {
		if (date == null)
			return null;
		return java.sql.Date.valueOf(date);
	}

	public static Date convert(LocalDateTime dateTime) {
		if (dateTime == null)
			return null;
		return java.sql.Timestamp.valueOf(dateTime);
	}

	public static Date addDays(Date date, int days) {
		Calendar c = GregorianCalendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}

	public static int getDayDist(Date d1, Date d2) {
		long dist = Math.abs(d1.getTime() - d2.getTime());
		return Math.round(dist / 86400000L); // DST: 25hrs will be rounded down
	}

	public static String formatIsoDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}
}
