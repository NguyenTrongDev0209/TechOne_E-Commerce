package com.techone.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.techone.controller.authentic.CustomOAuth2UserController;

@Configuration
public class SecurityConfig {
	@Autowired
    private CustomOAuth2UserController oauthUserService;
	
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            // 1. Cho phép truy cập công khai các file tĩnh và các trang chủ, chi tiết
	            .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/403").permitAll()
	            .requestMatchers("/product/product-detail", "/categories", "/promotions/**", "/posts/**").permitAll()
	            .requestMatchers("/forgot-password/**", "/verify-otp/**", "/reset-password/**").permitAll()

	            // 2. CHỈ ADMIN mới được vào các trang bắt đầu bằng /admin
	            .requestMatchers("/admin/**").hasRole("ADMIN")

	            // 3. Các trang bắt buộc phải đăng nhập (Bất kể ADMIN hay USER)
	            // Ví dụ: Thông tin tài khoản, Giỏ hàng, Thanh toán
	            .requestMatchers("/account/**", "/cart/**", "/checkout/**").authenticated()

	            // 4. Các yêu cầu còn lại đều phải xác thực
	            .anyRequest().authenticated()
	        )
	        .formLogin(login -> login.disable())
	        .oauth2Login(oauth -> oauth
	            .loginPage("/login")
	            .userInfoEndpoint(userInfo -> userInfo
	                .userService(oauthUserService)
	            )
	            .successHandler(successHandler())
	        )
	        // Cấu hình trang báo lỗi khi truy cập trái phép (tùy chọn)
	        .exceptionHandling(exception -> exception
	            .accessDeniedPage("/error-page") // Bạn có thể tạo trang 403.html đẹp mắt
	        );
	    
	    return http.build();
	}
	
	@Bean
	public AuthenticationSuccessHandler successHandler() {
	    return (request, response, authentication) -> {
	        // Kiểm tra xem trong danh sách quyền có ROLE_ADMIN không
	        boolean isAdmin = authentication.getAuthorities().stream()
	                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	        if (isAdmin) {
	            response.sendRedirect("/admin/dashboard");
	        } else {
	            response.sendRedirect("/");
	        }
	    };
	}
}
