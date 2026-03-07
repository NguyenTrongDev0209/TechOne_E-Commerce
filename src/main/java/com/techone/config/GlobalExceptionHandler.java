package com.techone.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "Tệp tin quá lớn! Vui lòng chọn tệp nhỏ hơn 50MB.");
        return "redirect:/admin/category-manager";
    }

    /**
     * Bắt tất cả URL không tồn tại (404) và redirect về trang /error-page
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFound(HttpServletResponse response) throws IOException {
        response.sendRedirect("/error-page");
    }
}
