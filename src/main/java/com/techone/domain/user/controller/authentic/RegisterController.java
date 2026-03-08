package com.techone.domain.user.controller.authentic;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.techone.domain.user.entity.Account;
import com.techone.domain.user.repository.AccountRepository;
import jakarta.validation.Valid;

@Controller
public class RegisterController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("account", new Account());
        return "views/authentic/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("fullname") String fullname,
            @RequestParam("password") String password,
            @RequestParam("contact") String contact,
            @RequestParam("register_method") String method,
            Model model, RedirectAttributes redirectAttributes,
            @Valid @ModelAttribute("account") Account account, BindingResult result) {

        if (result.hasErrors()) {
            return "views/authentic/register";
        }

        if ("email".equals(method)) {
            if (accountRepository.findByEmail(contact).isPresent()) {
                result.rejectValue("contact", "error.account", "Email này đã được đăng ký!");
                return "views/authentic/register";
            }
        } else {
            if (accountRepository.findByPhone(contact).isPresent()) {
                result.rejectValue("contact", "error.account", "Số điện thoại này đã được đăng ký!");
                return "views/authentic/register";
            }
        }

        try {
            Account newAccount = new Account();
            newAccount.setFullname(fullname);
            newAccount.setPassword(passwordEncoder.encode(password));
            newAccount.setRole(false);
            newAccount.setStatus(1);
            newAccount.setCreateAt(LocalDate.now());
            newAccount.setProvider("LOCAL");

            if ("email".equals(method)) {
                newAccount.setEmail(contact);
                newAccount.setPhone(null);
            } else {
                newAccount.setPhone(contact);
                newAccount.setEmail(null);
            }

            accountRepository.save(newAccount);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "views/authentic/register";
        }
    }
}
