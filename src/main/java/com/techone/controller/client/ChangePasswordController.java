package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.model.Account;
import com.techone.repository.AccountRepository;
import com.techone.utils.SessionUtils;

@Controller
public class ChangePasswordController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/account/change-password")
    public String changePasswordForm() {
        return "views/client/change-password";
    }

    @PostMapping("/account/change-password")
    public String changePassword(
            @RequestParam("current_password") String currentPassword,
            @RequestParam("new_password") String newPassword,
            @RequestParam("confirm_password") String confirmPassword,
            Model model) {

        // 1. Lấy thông tin người dùng hiện tại từ Session
        Account currentUser = SessionUtils.get("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 2. Kiểm tra mật khẩu hiện tại
        // Lưu ý: currentUser từ session có thể bị out-of-date, nên load lại từ DB
        Account account = accountRepository.findById(currentUser.getId()).orElse(null);
        if (account == null) {
            model.addAttribute("error", "Không tìm thấy thông tin tài khoản!");
            return "views/client/change-password";
        }

        if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
            model.addAttribute("error", "Mật khẩu hiện tại không chính xác!");
            return "views/client/change-password";
        }

        // 3. Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Xác nhận mật khẩu mới không khớp!");
            return "views/client/change-password";
        }

        if (newPassword.length() < 8) {
            model.addAttribute("error", "Mật khẩu mới phải có ít nhất 8 ký tự!");
            return "views/client/change-password";
        }

        // 4. Cập nhật mật khẩu mới
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        // Cập nhật lại user trong session
        SessionUtils.set("user", account);

        model.addAttribute("success", "Đổi mật khẩu thành công!");
        return "views/client/change-password";
    }
}
