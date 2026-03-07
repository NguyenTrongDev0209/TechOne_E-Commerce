package com.techone.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CookieUtils {

    /**
     * Tạo và gửi Cookie về client
     * @param name Tên cookie
     * @param value Giá trị
     * @param hours Thời gian tồn tại (giờ)
     * @param response Đối tượng response để gửi cookie
     */
    public static void add(String name, String value, int hours, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(hours * 60 * 60); // Đổi giờ ra giây
        cookie.setPath("/"); // Cookie có hiệu lực trên toàn bộ website
        response.addCookie(cookie);
    }

    /**
     * Đọc giá trị Cookie
     * @param name Tên cookie cần đọc
     * @return Giá trị cookie hoặc chuỗi rỗng nếu không tìm thấy
     */
    public static String get(String name) {
        // Lấy request hiện tại tự động từ Spring Context
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase(name)) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    /**
     * Xóa Cookie
     * @param name Tên cookie cần xóa
     * @param response Đối tượng response để gửi yêu cầu xóa
     */
    public static void remove(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null); // Ghi đè giá trị null
        cookie.setMaxAge(0); // Set thời gian sống = 0 để xóa ngay lập tức
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
