package com.techone.controller.authentic;

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

import com.techone.model.Account;
import com.techone.repository.AccountRepository;

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
	
//	@PostMapping("/register")
//	public String register(@RequestParam("fullname") String fullname,
//            @RequestParam("password") String password,
//            @RequestParam("contact") String contact,
//            @RequestParam("register_method") String method,
//            Model model, RedirectAttributes redirectAttributes,
//            @Valid @ModelAttribute("account") Account account, BindingResult result) {
//		try {
//			// 1. Bây giờ result.hasErrors() sẽ bắt được cả lỗi trống của 'contact'
//		    if (result.hasErrors()) {
//		        return "views/authentic/register";
//		    }
//
//		    String contactValue = account.getContact(); // Lấy giá trị từ field ảo
//
//		    // 2. Logic kiểm tra trùng lặp
//		    if ("email".equals(method)) {
//		        if (accountRepository.findByEmail(contactValue).isPresent()) {
//		            // Gán lỗi thủ công vào field 'contact' để hiển thị dưới input
//		            result.rejectValue("contact", "error.account", "Email này đã được đăng ký!");
//		            return "views/authentic/register";
//		        }
//		        account.setEmail(contactValue);
//		    } else {
//		        if (accountRepository.findByPhone(contactValue).isPresent()) {
//		            result.rejectValue("contact", "error.account", "Số điện thoại này đã được đăng ký!");
//		            return "views/authentic/register";
//		        }
//		        account.setPhone(contactValue);
//		    }
//			
//		    Account newAccount = new Account();
//		    newAccount.setFullname(fullname);
//		    newAccount.setPassword(passwordEncoder.encode(password));
//
//		    // Xử lý giá trị giữ chỗ để tránh lỗi UNIQUE NULL trong SQL Server
//		    String timestamp = String.valueOf(System.currentTimeMillis());
//
//		    if ("email".equals(method)) {
//		        newAccount.setEmail(contact);
//		        // Thay vì null, gán một mã định danh duy nhất cho phone
//		        newAccount.setPhone(null); 
//		    } else {
//		        newAccount.setPhone(contact);
//		        // Thay vì null, gán một mã định danh duy nhất cho email
//		        newAccount.setEmail("no_email_" + timestamp + "@techone.com");
//		    }
//
//		    newAccount.setRole(false);
//		    newAccount.setStatus(1);
//		    newAccount.setCreateAt(LocalDate.now());
//		    newAccount.setProvider("LOCAL");
//
//		    accountRepository.save(newAccount);
//		    redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
//		    return "redirect:/login";
//
//		} catch (Exception e) {
//		    redirectAttributes.addFlashAttribute("error", "Đăng ký thất bại");
//		    return "redirect:/register";
//		}
//    }
	@PostMapping("/register")
	public String register(@RequestParam("fullname") String fullname,
	            @RequestParam("password") String password,
	            @RequestParam("contact") String contact,
	            @RequestParam("register_method") String method,
	            Model model, RedirectAttributes redirectAttributes,
	            @Valid @ModelAttribute("account") Account account, BindingResult result) {
	    
	    // 1. Kiểm tra lỗi validate cơ bản (fullname, password...)
	    if (result.hasErrors()) {
	        return "views/authentic/register";
	    }

	    // 2. Kiểm tra trùng lặp trong DB trước khi lưu
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
	        // 3. Tạo đối tượng mới và gán giá trị
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
