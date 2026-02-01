package com.techone.utils;

import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

public class AppUtils {

    // ==========================================================
    // 1. TIỆN ÍCH CHUỖI (STRING)
    // ==========================================================

    /**
     * Tạo Slug từ tiêu đề (SEO Friendly)
     * Input: "  Lập Trình   Java  " -> Output: "lap-trinh-java"
     */
    public static String toSlug(String input) {
        if (input == null) return "";
        
        String nowhitespace = Pattern.compile("[\\s]").matcher(input.trim()).replaceAll("-");
        
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        
        return pattern.matcher(normalized).replaceAll("").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-");
    }

    /**
     * Viết hoa chữ cái đầu mỗi từ
     * Sử dụng StringBuilder để tối ưu hiệu năng
     * Input: "lê  quốc   minh" -> Output: "Lê Quốc Minh"
     */
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return "";

        // Chuyển hết về chữ thường và tách từ dựa trên khoảng trắng (regex \\s+)
        String[] words = input.toLowerCase().trim().split("\\s+");
        
        // Dùng StringBuilder thay vì cộng chuỗi (+) trong vòng lặp
        StringBuilder sb = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                // Viết hoa ký tự đầu
                char firstChar = Character.toTitleCase(word.charAt(0));
                // Lấy phần còn lại
                String rest = word.substring(1);
                
                sb.append(firstChar).append(rest).append(" ");
            }
        }
        return sb.toString().trim();
    }

    // ==========================================================
    // 2. TIỆN ÍCH THỜI GIAN (TIME)
    // ==========================================================

    /**
     * Format ngày giờ: 01/02/2026 15:30
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Format chỉ lấy ngày: 01/02/2026
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Thời gian tương đối: "5 phút trước", "2 giờ trước"
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        
        long seconds = duration.getSeconds();
        if (seconds < 60) return "Vừa xong";
        
        long minutes = duration.toMinutes();
        if (minutes < 60) return minutes + " phút trước";
        
        long hours = duration.toHours();
        if (hours < 24) return hours + " giờ trước";
        
        long days = duration.toDays();
        if (days == 1) return "Hôm qua";
        if (days < 7) return days + " ngày trước";
        
        return formatDate(dateTime);
    }

    // ==========================================================
    // 3. TIỆN ÍCH BÁN HÀNG (E-COMMERCE)
    // ==========================================================

    /**
     * Định dạng tiền tệ VN: 15.000.000 ₫
     */
    public static String formatCurrency(Double amount) {
        if (amount == null) return "0 ₫";
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }

    /**
     * Tính % giảm giá: (Giá gốc - Giá bán) / Giá gốc * 100
     */
    public static int calculateDiscountPercent(Double originalPrice, Double salePrice) {
        if (originalPrice == null || salePrice == null || originalPrice == 0) return 0;
        double discount = ((originalPrice - salePrice) / originalPrice) * 100;
        return (int) Math.round(discount);
    }
}
