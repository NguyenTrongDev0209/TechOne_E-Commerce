package com.techone.controller.authentic;

import java.time.LocalDateTime;
import java.util.Optional;

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
import com.techone.utils.SessionUtils;

@Controller
public class ConfirmOtpController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @GetMapping("/confirm-otp")
    public String showConfirmOtp() {
        String email = SessionUtils.get("forgotPasswordEmail");
        if (email == null) {
            return "redirect:/forgot-password";
        }
        return "views/authentic/confirm-otp";
    }

    @PostMapping("/confirm-otp")
    public String verifyOtp(@RequestParam("otp") String otp, Model model) {
        String email = SessionUtils.get("forgotPasswordEmail");
        if (email == null) {
            return "redirect:/forgot-password";
        }

        Optional<Account> userOpt = accountRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "redirect:/forgot-password";
        }

        Account account = userOpt.get();
        Optional<ForgotPassword> fpOpt = forgotPasswordRepository.findByOtpAndAccount(otp, account);

        if (fpOpt.isEmpty()) {
            model.addAttribute("error", "Mã OTP không chính xác");
            return "views/authentic/confirm-otp";
        }

        ForgotPassword fp = fpOpt.get();
        if (fp.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Mã OTP đã hết hạn. Vui lòng gửi lại.");
            return "views/authentic/confirm-otp";
        }

        // OTP is valid
        SessionUtils.set("otpVerified", true);
        return "redirect:/reset-password";
    }
}
