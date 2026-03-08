package com.techone.domain.user.controller.client;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.techone.domain.user.entity.Account;
import com.techone.domain.user.repository.AccountRepository;
import com.techone.common.utils.SessionUtils;
import jakarta.servlet.ServletContext;

@Controller
public class ProfileController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ServletContext servletContext;

    @org.springframework.beans.factory.annotation.Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping("/account/profile")
    public String showProfile() {
        Account user = SessionUtils.get("user");
        if (user == null) {
            return "redirect:/login";
        }
        return "views/client/profile";
    }

    @PostMapping("/account/profile/update")
    public String updateProfile(@ModelAttribute Account accountForm,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            RedirectAttributes redirectAttributes) {

        Account currentUser = SessionUtils.get("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Account> optAccount = accountRepository.findById(currentUser.getId());
        if (optAccount.isPresent()) {
            Account account = optAccount.get();
            account.setFullname(accountForm.getFullname());
            account.setBirthday(accountForm.getBirthday());
            account.setGender(accountForm.getGender());
            account.setPhone(accountForm.getPhone());

            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    String fileName = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                    String avatarsDir = uploadDir + "/images/avatars/";
                    File dir = new File(avatarsDir);
                    if (!dir.exists())
                        dir.mkdirs();
                    Path path = Paths.get(avatarsDir + fileName);
                    Files.copy(avatarFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    account.setAvatar(fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "Lỗi khi tải ảnh lên!");
                    return "redirect:/account/profile";
                }
            }
            accountRepository.save(account);
            SessionUtils.set("user", account);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản!");
        }
        return "redirect:/account/profile";
    }
}
