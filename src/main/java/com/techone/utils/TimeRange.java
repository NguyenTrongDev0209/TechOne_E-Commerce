package com.techone.utils;

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

    /**
     * Hôm nay
     * Ví dụ: 2026-02-01 00:00 -> 2026-02-02 00:00
     */
    public static TimeRange today() {
        LocalDate now = LocalDate.now();
        return new TimeRange(now, now.plusDays(1));
    }

    /**
     * Tuần này (Bắt đầu từ Thứ 2)
     */
    public static TimeRange thisWeek() {
        LocalDate now = LocalDate.now();
        
        LocalDate begin = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return new TimeRange(begin, begin.plusDays(7));
    }

    /**
     * Tháng này
     */
    public static TimeRange thisMonth() {
        LocalDate now = LocalDate.now();
        
        LocalDate begin = now.withDayOfMonth(1);
        
        LocalDate end = begin.plusMonths(1); 
        return new TimeRange(begin, end);
    }

    /**
     * Quý này (3 tháng)
     */
    public static TimeRange thisQuarter() {
        LocalDate now = LocalDate.now();
        
        int firstMonthOfQuarter = now.getMonth().firstMonthOfQuarter().getValue();
        LocalDate begin = now.withMonth(firstMonthOfQuarter).withDayOfMonth(1);
        return new TimeRange(begin, begin.plusMonths(3));
    }

    /**
     * Năm này
     */
    public static TimeRange thisYear() {
        LocalDate now = LocalDate.now();
        LocalDate begin = now.withMonth(1).withDayOfMonth(1);
        return new TimeRange(begin, begin.plusYears(1));
    }
}
