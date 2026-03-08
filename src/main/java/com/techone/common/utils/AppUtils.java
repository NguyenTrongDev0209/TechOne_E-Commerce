package com.techone.common.utils;

import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

public class AppUtils {

    public static String toSlug(String input) {
        if (input == null)
            return "";
        String nowhitespace = Pattern.compile("[\\s]").matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-");
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty())
            return "";
        String[] words = input.toLowerCase().trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                char firstChar = Character.toTitleCase(word.charAt(0));
                String rest = word.substring(1);
                sb.append(firstChar).append(rest).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null)
            return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return "";
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        long seconds = duration.getSeconds();
        if (seconds < 60)
            return "Vừa xong";
        long minutes = duration.toMinutes();
        if (minutes < 60)
            return minutes + " phút trước";
        long hours = duration.toHours();
        if (hours < 24)
            return hours + " giờ trước";
        long days = duration.toDays();
        if (days == 1)
            return "Hôm qua";
        if (days < 7)
            return days + " ngày trước";
        return formatDate(dateTime);
    }

    public static String formatCurrency(Double amount) {
        if (amount == null)
            return "0 ₫";
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(amount);

    }

    public static int calculateDiscountPercent(Double originalPrice, Double salePrice) {
        if (originalPrice == null || salePrice == null || originalPrice == 0)
            return 0;
        double discount = ((originalPrice - salePrice) / originalPrice) * 100;
        return (int) Math.round(discount);
    }
}
