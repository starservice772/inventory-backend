package com.starservice.inventory.inventory_app.utility;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class ReportDateUtil {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    public static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter REPORT_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private ReportDateUtil() {
    }

    public record InstantRange(Instant startInclusive, Instant endInclusive) {
    }

    public static InstantRange toInstantRange(String fromDate, String toDate) {
        if (fromDate == null || fromDate.isBlank()) {
            throw new IllegalArgumentException("fromDate is required");
        }
        if (toDate == null || toDate.isBlank()) {
            throw new IllegalArgumentException("toDate is required");
        }

        LocalDate from = parseReportDate(fromDate.trim(), "fromDate");
        LocalDate to = parseReportDate(toDate.trim(), "toDate");

        if (to.isBefore(from)) {
            throw new IllegalArgumentException("toDate must be on or after fromDate");
        }

        Instant startInclusive = from.atStartOfDay(IST).toInstant();
        Instant endInclusive = to.atTime(LocalTime.MAX).atZone(IST).toInstant();

        return new InstantRange(startInclusive, endInclusive);
    }

    public static String formatInstantDate(Instant instant) {
        if (instant == null) {
            return "";
        }
        return instant.atZone(IST).format(REPORT_DATE_FORMAT);
    }

    public static String formatInstantDateTime(Instant instant) {
        if (instant == null) {
            return "";
        }
        return instant.atZone(IST).format(REPORT_DATE_TIME_FORMAT);
    }

    public static String currentIstFileDate() {
        return Instant.now().atZone(IST).format(FILE_DATE_FORMAT);
    }

    public static String currentIstDateTimeLabel() {
        return Instant.now().atZone(IST).format(REPORT_DATE_TIME_FORMAT) + " IST";
    }

    private static LocalDate parseReportDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value, REPORT_DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    fieldName + " must be in dd-MM-yyyy format: " + value);
        }
    }
}
