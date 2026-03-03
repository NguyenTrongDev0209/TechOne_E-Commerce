package com.techone.controller.authentic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.model.Account;
import com.techone.repository.AccountRepository;
import com.techone.utils.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LoginController {

	@Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
   // private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
	
    @Autowired
    private SecurityContextRepository securityContextRepository;

	@GetMapping("/login")
	public String loginForm() {
		return "views/authentic/login";
	}

	@PostMapping("/login")
	public String login(@RequestParam("username") String username, 
	                    @RequestParam("password") String password, 
	                    HttpServletRequest request, 
	                    HttpServletResponse response, 
	                    Model model) {
	    
	    if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
	        model.addAttribute("error", "Vui lòng nhập đầy đủ email/số điện thoại và mật khẩu");
	        return "views/authentic/login";
	    }
	    
	    Optional<Account> userOpt = accountRepository.findByEmailOrPhone(username, username);
	    if (userOpt.isPresent()) {
	        Account account = userOpt.get();
	        if (passwordEncoder.matches(password, account.getPassword())) {
	            
	            // 1. Lưu session cho UI
	            com.techone.utils.SessionUtils.set("user", account);

	            // Tạo danh sách quyền (Roles)
	            List<GrantedAuthority> authorities = new ArrayList<>();
	            authorities.add(new SimpleGrantedAuthority(account.getRole() ? "ROLE_ADMIN" : "ROLE_USER"));
	            UsernamePasswordAuthenticationToken auth = 
	                new UsernamePasswordAuthenticationToken(username, null, authorities);

	            // 3. QUAN TRỌNG: Lưu vào SecurityContext và PERSIST (lưu bền vững) vào Session
	            SecurityContext context = SecurityContextHolder.createEmptyContext();
	            context.setAuthentication(auth);
	            SecurityContextHolder.setContext(context);
	            securityContextRepository1.saveContext(context, request, response);

	            if (account.getRole()) {
	                return "redirect:/admin/dashboard";
	            }
	            return "redirect:/";
	        }
	    }

	    model.addAttribute("error", "Tài khoản hoặc mật khẩu không chính xác");
	    return "views/authentic/login";
	}
    
}
