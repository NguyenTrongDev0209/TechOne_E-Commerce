package com.techone.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeRange {

    private Date begin;
    private Date end;

    private TimeRange(LocalDate begin, LocalDate end) {
        this.begin = java.sql.Date.valueOf(begin);
        this.end = java.sql.Date.valueOf(end);
    }

    public static TimeRange today() {
        LocalDate now = LocalDate.now();
        return new TimeRange(now, now.plusDays(1));
    }

    public static TimeRange thisWeek() {
        LocalDate now = LocalDate.now();
        LocalDate begin = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return new TimeRange(begin, begin.plusDays(7));
    }

    public static TimeRange thisMonth() {
        LocalDate now = LocalDate.now();
        LocalDate begin = now.withDayOfMonth(1);
        LocalDate end = begin.plusMonths(1);
        return new TimeRange(begin, end);
    }

    public static TimeRange thisQuarter() {
        LocalDate now = LocalDate.now();
        int firstMonthOfQuarter = now.getMonth().firstMonthOfQuarter().getValue();
        LocalDate begin = now.withMonth(firstMonthOfQuarter).withDayOfMonth(1);
        return new TimeRange(begin, begin.plusMonths(3));
    }

    public static TimeRange thisYear() {
        LocalDate now = LocalDate.now();
        LocalDate begin = now.withMonth(1).withDayOfMonth(1);
        return new TimeRange(begin, begin.plusYears(1));
    }
}
