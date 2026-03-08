package com.techone.domain.user.controller.authentic;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import com.techone.domain.user.entity.Account;
import com.techone.domain.user.entity.ForgotPassword;
import com.techone.domain.user.repository.AccountRepository;
import com.techone.domain.user.repository.ForgotPasswordRepository;
import com.techone.common.utils.SessionUtils;

@Controller
public class ConfirmOtpController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @GetMapping("/confirm-otp")
    public String showConfirmOtp() {
        String email = SessionUtils.get("forgotPasswordTarget");
        if (email == null)
            return "redirect:/forgot-password";
        return "views/authentic/confirm-otp";
    }

    @PostMapping("/confirm-otp")
    public String verifyOtp(@RequestParam("otp") String otp, Model model) {
        String email = SessionUtils.get("forgotPasswordTarget");
        if (email == null)
            return "redirect:/forgot-password";

        String method = SessionUtils.get("forgotPasswordMethod");
        Optional<Account> userOpt = "EMAIL".equalsIgnoreCase(method) ? accountRepository.findByEmail(email)
                : accountRepository.findByPhone(email);

        if (userOpt.isEmpty())
            return "redirect:/forgot-password";

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

        SessionUtils.set("otpVerified", true);
        return "redirect:/reset-password";
    }

    @PostMapping("/confirm-otp/api/verify-success")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyPhoneSuccess() {
        Map<String, Object> response = new HashMap<>();
        SessionUtils.set("otpVerified", true);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
