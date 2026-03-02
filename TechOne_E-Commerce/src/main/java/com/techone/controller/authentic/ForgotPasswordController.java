package com.techone.controller.authentic;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.model.Account;
import com.techone.model.ForgotPassword;
import com.techone.repository.AccountRepository;
import com.techone.repository.ForgotPasswordRepository;
import com.techone.service.MailerService;
import com.techone.utils.SessionUtils;

import jakarta.mail.MessagingException;

@Controller
public class ForgotPasswordController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Autowired
    private MailerService mailService;

    @GetMapping("/forgot-password")
    public String form() {
        return "views/authentic/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendOtp(@RequestParam("email") String email, Model model) {
        Optional<Account> userOpt = accountRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống");
            return "views/authentic/forgot-password";
        }

        Account account = userOpt.get();
        String otp = String.format("%06d", new Random().nextInt(999999));

        ForgotPassword forgotPassword = forgotPasswordRepository.findByAccount(account).orElse(new ForgotPassword());
        forgotPassword.setOtp(otp);
        forgotPassword.setAccount(account);
        forgotPassword.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        forgotPasswordRepository.save(forgotPassword);

        try {
            mailService.send(email, "Mã xác thực đặt lại mật khẩu", "Mã xác thực của bạn là: <b>" + otp + "</b>. Mã có hiệu lực trong 5 phút.");
            SessionUtils.set("forgotPasswordEmail", email);
            return "redirect:/confirm-otp";
        } catch (MessagingException e) {
            model.addAttribute("error", "Lỗi gửi email: " + e.getMessage());
            return "views/authentic/forgot-password";
        }
    }
}
