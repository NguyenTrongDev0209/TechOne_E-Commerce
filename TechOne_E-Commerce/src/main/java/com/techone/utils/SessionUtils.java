package com.techone.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SessionUtils {

    /**
     * Lưu giá trị vào Session
     * @param name Tên attribute
     * @param value Giá trị (Object bất kỳ: String, User, Cart...)
     */
    public static void set(String name, Object value) {
        HttpSession session = getSession();
        session.setAttribute(name, value);
    }

    /**
     * Lấy giá trị từ Session
     * @param name Tên attribute cần lấy
     * @return Giá trị ép kiểu về T (Generic)
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String name) {
        HttpSession session = getSession();
        return (T) session.getAttribute(name);
    }

    /**
     * Xóa attribute khỏi Session
     * @param name Tên attribute cần xóa
     */
    public static void remove(String name) {
        HttpSession session = getSession();
        session.removeAttribute(name);
    }

    /**
     * Hủy toàn bộ Session (Đăng xuất)
     */
    public static void invalidate() {
        HttpSession session = getSession();
        session.invalidate();
    }
    
    /**
     * Hàm nội bộ để lấy HttpSession hiện tại
     */
    private static HttpSession getSession() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getSession();
    }
}