package com.tbscg.doosan.magnolia.mediatracker.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DateFormatUtilTest {

    @Test
    public void shouldGetLocalDateFromOffsetDateTime() {
        OffsetDateTime date = OffsetDateTime.of(2021,
                9,
                21,
                9,
                5,
                30,
                0,
                OffsetDateTime.now().getOffset());
        LocalDate expectedDate = LocalDate.of(2021, 9, 21);

        LocalDate localDate = DateFormatUtil.getLocalDateFromOffsetDateTime(date);

        assertEquals(expectedDate, localDate);
    }

    @Test
    public void shouldReturnNullWhenOffsetDateTimeNull() {
        LocalDate localDate = DateFormatUtil.getLocalDateFromOffsetDateTime(null);

        assertNull(localDate);
    }

    @Test
    public void shouldGetOffsetDateTimeFromLocalDate() {
        LocalDate localDate = LocalDate.of(2021, 9, 21);
        OffsetDateTime expected = OffsetDateTime.of(2021,
                9,
                21,
                0,
                0,
                0,
                0,
                OffsetDateTime.now().getOffset());

        OffsetDateTime offsetDateTime = DateFormatUtil.getOffsetDateTimeFromLocalDate(localDate);


        assertEquals(expected, offsetDateTime);
    }

    @Test
    public void shouldReturnNullWhenLocalDateNull() {
        OffsetDateTime offsetDateTime = DateFormatUtil.getOffsetDateTimeFromLocalDate(null);

        assertNull(offsetDateTime);
    }

    @Test
    public void shouldGetGivenDateWhenBeforeCurrent() {
        OffsetDateTime offsetDateTime = OffsetDateTime.now().minusDays(1);

        OffsetDateTime date = DateFormatUtil.getDateIfBeforeOrCurrent(offsetDateTime);

        assertEquals(offsetDateTime, date);
    }

    @Test
    public void shouldGetCurrentDateWhenAfterCurrent() {
        OffsetDateTime offsetDateTime = OffsetDateTime.now().plusDays(1);

        OffsetDateTime date = DateFormatUtil.getDateIfBeforeOrCurrent(offsetDateTime);

        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(date));
    }

    @Test
    public void shouldGetCurrentDateWhenNull() {
        OffsetDateTime date = DateFormatUtil.getDateIfBeforeOrCurrent(null);

        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(date));
    }

    protected OffsetDateTime castDateToDays(OffsetDateTime now) {
        return now.truncatedTo(ChronoUnit.DAYS);
    }

}