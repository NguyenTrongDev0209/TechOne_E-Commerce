package com.techone.controller.authentic;

import com.techone.model.Account;
import com.techone.repository.AccountRepository;
import com.techone.utils.SessionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

@Service
public class CustomOAuth2UserController extends DefaultOAuth2UserService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        Optional<Account> accountOptional = accountRepository.findByEmail(email);

        Account account;
        if (accountOptional.isEmpty()) {
            // Nếu chưa có tài khoản thì tạo mới
            Account newAccount = new Account();
            newAccount.setEmail(email);
            newAccount.setFullname(name);
            newAccount.setAvatar(picture);
            newAccount.setProvider("GOOGLE"); // Đánh dấu nguồn từ Google
            newAccount.setStatus(1);
            newAccount.setRole(false);
            newAccount.setCreateAt(LocalDate.now());
            // Password để trống hoặc random vì login qua Google không cần pass local
            newAccount.setPassword("OAUTH2_USER_" + System.currentTimeMillis());
            
            account = accountRepository.save(newAccount);
        } else {
            account = accountOptional.get();
            // Cập nhật thông tin từ Google nếu cần (ví dụ avatar)
            if (picture != null && !picture.equals(account.getAvatar())) {
                account.setAvatar(picture);
                accountRepository.save(account);
            }
        }

        // Lưu thông tin account vào session để hiển thị trên UI
        SessionUtils.set("user", account);

        // Thêm quyền từ database vào OAuth2User
        Set<GrantedAuthority> authorities = new LinkedHashSet<>(oAuth2User.getAuthorities());
        authorities.add(new SimpleGrantedAuthority(account.getRole() ? "ROLE_ADMIN" : "ROLE_USER"));
        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");

    }
}
