package com.techone.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SessionUtils {

    public static void set(String name, Object value) {
        HttpSession session = getSession();
        session.setAttribute(name, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String name) {
        HttpSession session = getSession();
        return (T) session.getAttribute(name);
    }

    public static void remove(String name) {
        HttpSession session = getSession();
        session.removeAttribute(name);
    }

    public static void invalidate() {
        HttpSession session = getSession();
        session.invalidate();
    }

    private static HttpSession getSession() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        return request.getSession();
    }
}
