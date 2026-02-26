package com.techone.controller.authentic;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.model.Account;
import com.techone.model.ForgotPassword;
import com.techone.repository.AccountRepository;
import com.techone.repository.ForgotPasswordRepository;
import com.techone.utils.SessionUtils;

@Controller
public class ResetPasswordController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/reset-password")
    public String showResetPage() {
        Boolean verified = SessionUtils.get("otpVerified");
        if (verified == null || !verified) {
            return "redirect:/forgot-password";
        }
        return "views/authentic/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("password") String password, 
                               @RequestParam("confirmPassword") String confirmPassword, 
                               Model model) {
        Boolean verified = SessionUtils.get("otpVerified");
        String email = SessionUtils.get("forgotPasswordEmail");

        if (verified == null || !verified || email == null) {
            return "redirect:/forgot-password";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự");
            return "views/authentic/reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "views/authentic/reset-password";
        }

        Optional<Account> userOpt = accountRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            Account account = userOpt.get();
            account.setPassword(passwordEncoder.encode(password));
            accountRepository.save(account);

            // Clean up
            Optional<ForgotPassword> fpOpt = forgotPasswordRepository.findByAccount(account);
            fpOpt.ifPresent(fp -> forgotPasswordRepository.delete(fp));
            SessionUtils.remove("otpVerified");
            SessionUtils.remove("forgotPasswordEmail");

            model.addAttribute("success", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.");
            return "redirect:/login"; 
        }

        return "redirect:/forgot-password";
    }
}
