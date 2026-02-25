package com.techone.controller.authentic;

import com.techone.model.Account;
import com.techone.model.ForgotPassword;
import com.techone.repository.AccountRepository;
import com.techone.repository.ForgotPasswordRepository;
import com.techone.service.MailerService;
import com.techone.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Controller
public class ForgotPasswordController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Autowired
    private MailerService mailerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "views/authentic/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);

        if (accountOpt.isEmpty()) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống!");
            return "views/authentic/forgot-password";
        }

        Account account = accountOpt.get();

        // Tạo mã OTP 6 số
        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        // Lưu hoặc cập nhật OTP vào database
        ForgotPassword forgotPassword = forgotPasswordRepository.findByAccount(account)
                .orElse(new ForgotPassword());
        
        forgotPassword.setAccount(account);
        forgotPassword.setOtp(otp);
        forgotPassword.setExpiryDate(LocalDateTime.now().plusMinutes(5)); // Hết hạn sau 5 phút
        
        forgotPasswordRepository.save(forgotPassword);

        // Gửi email
        String subject = "Mã xác nhận thay đổi mật khẩu - TechOne";
        String body = "Chào " + account.getFullname() + ",\n\n"
                    + "Mã xác nhận (OTP) của bạn là: " + otp + "\n"
                    + "Mã này có hiệu lực trong 5 phút. Vui lòng không cung cấp mã này cho bất kỳ ai.\n\n"
                    + "Trân trọng,\nTechOne Support Team";

        try {
            mailerService.sendEmail(email, subject, body);
            SessionUtils.set("resetEmail", email);
            return "redirect:/verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi gửi email: " + e.getMessage());
            return "views/authentic/forgot-password";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm() {
        if (SessionUtils.get("resetEmail") == null) return "redirect:/forgot-password";
        return "views/authentic/confirm-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp, Model model) {
        String email = SessionUtils.get("resetEmail");
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            Optional<ForgotPassword> fpOpt = forgotPasswordRepository.findByOtpAndAccount(otp, account);
            
            if (fpOpt.isPresent() && fpOpt.get().getExpiryDate().isAfter(LocalDateTime.now())) {
                SessionUtils.set("otpVerified", true);
                return "redirect:/reset-password";
            }
        }
        
        model.addAttribute("error", "Mã OTP không chính xác hoặc đã hết hạn!");
        return "views/authentic/verify-otp";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        if (SessionUtils.get("otpVerified") == null) return "redirect:/forgot-password";
        return "views/authentic/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("password") String password, 
                                @RequestParam("confirmPassword") String confirmPassword, 
                                Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "views/authentic/reset-password";
        }

        String email = SessionUtils.get("resetEmail");
        Account account = accountRepository.findByEmail(email).get();
        
        account.setPassword(passwordEncoder.encode(password));
        accountRepository.save(account);
        
        // Xóa OTP sau khi dùng xong
        forgotPasswordRepository.findByAccount(account).ifPresent(forgotPasswordRepository::delete);
        
        SessionUtils.remove("resetEmail");
        SessionUtils.remove("otpVerified");
        
        return "redirect:/login?resetSuccess=true";
    }
}
