package com.tbscg.doosan.magnolia.dxp.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	public static Calendar parseUTCDate(String dateStr) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		dateFormat.setTimeZone(UTC);
		Date date = dateFormat.parse(dateStr);
		Calendar calendar = Calendar.getInstance(UTC);
		calendar.setTime(date);
		return calendar;
	}
}
