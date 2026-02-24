package com.techone.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
            .csrf(csrf -> csrf.disable()) // Tạm thời disable để test form submit dễ hơn
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/", "/product/product-detail",
                		"/posts/**", "/account/**","/promotions/**", "/categories","/css/**", "/js/**", "/images/**").permitAll() // Cho phép truy cập công khai
				.anyRequest().authenticated()
            )
//            .formLogin(login -> login
//                .loginPage("/login") 
//                .permitAll()
//            )
            .formLogin(login -> login.disable())
        	.oauth2Login(oauth -> oauth
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oauthUserService) 
                )
                .defaultSuccessUrl("/", true) // Chuyển hướng sau khi login thành công
            );
        
        return http.build();
    }
}
