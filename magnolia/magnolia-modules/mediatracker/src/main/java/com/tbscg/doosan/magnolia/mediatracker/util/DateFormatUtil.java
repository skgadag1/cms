package com.tbscg.doosan.magnolia.mediatracker.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Util class containing static methods to operate on dates
 */
public class DateFormatUtil {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSXX");
    private static final DateTimeFormatter JUST_DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    /**
     * Formats date into ISO format
     *
     * @param date OffsetDateTime object to format
     * @return date as formatted string
     */
    public static String formatOffsetDateTime(OffsetDateTime date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
    }

    /**
     * Maps OffsetDateTime to LocalDate object
     *
     * @param date OffsetDateTime object to map
     * @return date as LocalDate or null if given date cannot be parsed
     */
    public static LocalDate getLocalDateFromOffsetDateTime(OffsetDateTime date) {
        try {
            return LocalDate.parse(formatOffsetDateTime(date));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if given date is before current time, if not returns current time
     *
     * @param date date to check
     * @return given date or current time as OffsetDateTime object
     */
    public static OffsetDateTime getDateIfBeforeOrCurrent(OffsetDateTime date) {
        OffsetDateTime currentTime = OffsetDateTime.now();
        return (date == null || date.isAfter(currentTime)) ? currentTime : date;

    }

    /**
     * Maps LocalDate to OffsetDateTime with default offset from UTC/Greenwich
     *
     * @param date LocalDate object to map
     * @return date in OffsetDateTime format
     */
    public static OffsetDateTime getOffsetDateTimeFromLocalDate(LocalDate date) {
        if (date != null) {
            return OffsetDateTime.of(date.atStartOfDay(), OffsetDateTime.now().getOffset());
        }
        return null;
    }

    /**
     * Formats datetime into ISO format and concatenates it with user name
     *
     * @param time datetime object
     * @param user user name
     * @return formatted datetime object with user name
     */
    public static String formatTimestamp(LocalDateTime time, String user) {
        StringBuilder s = new StringBuilder();
        if (time != null) {
            s.append(time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            if (StringUtils.isNotEmpty(user)) {
                s.append(" by ");
                s.append(user);
            }
        }
        return s.toString();
    }

    /**
     * Generates default end date datetime
     * @return endDate object
     */
    public static OffsetDateTime getDefaultEndDate() {
        return OffsetDateTime.of(
                LocalDateTime.of(2099, 12, 31, 00, 00),
                ZoneOffset.UTC);
    }

    /**
     * Extracts date from datetime object
     *
     * @param offsetDateTime object to extract from
     * @return date as String
     */
    public static String showDateOnly(OffsetDateTime offsetDateTime) {
        if(offsetDateTime != null)
            return offsetDateTime.format(JUST_DATE_FORMATTER);
        else
            return "";
    }

}
