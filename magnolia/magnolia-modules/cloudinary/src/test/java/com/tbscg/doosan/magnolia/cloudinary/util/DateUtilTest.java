package com.tbscg.doosan.magnolia.cloudinary.util;

import com.tbscg.doosan.magnolia.dxp.core.util.DateUtil;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DateUtilTest {

	@Test
	public void testParseUTCDate_invalidFormat() {
		String dateStr = "2019-06-13";
		try {
			DateUtil.parseUTCDate(dateStr);
			fail("should've thrown a ParseException for string " + dateStr);
		} catch (ParseException e) {
		}
	}

	@Test
	public void testParseUTCDate() {
		String dateStr = "2007-06-05T04:03:02+01:00";
		try {
			Calendar date = DateUtil.parseUTCDate(dateStr);

			assertEquals(2007, date.get(Calendar.YEAR));
			assertEquals(Calendar.JUNE, date.get(Calendar.MONTH));
			assertEquals(5, date.get(Calendar.DAY_OF_MONTH));
			assertEquals(3, date.get(Calendar.HOUR_OF_DAY));
			assertEquals(3, date.get(Calendar.MINUTE));
			assertEquals(2, date.get(Calendar.SECOND));

			assertEquals("UTC", date.getTimeZone().getID());
			assertEquals(0, date.get(Calendar.DST_OFFSET));
		} catch (ParseException e) {
			fail("Exception thrown: " + e);
		}
	}
}
