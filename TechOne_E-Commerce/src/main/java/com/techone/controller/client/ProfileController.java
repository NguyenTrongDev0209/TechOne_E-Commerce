package com.techone.controller.client;

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

import com.techone.model.Account;
import com.techone.repository.AccountRepository;
import com.techone.utils.SessionUtils;

import jakarta.servlet.ServletContext;

@Controller
public class ProfileController {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private ServletContext servletContext;
	
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
			
			// Update basic info
			account.setFullname(accountForm.getFullname());
			account.setBirthday(accountForm.getBirthday());
			account.setGender(accountForm.getGender());
			account.setPhone(accountForm.getPhone());
			
			// Handle avatar upload
			if (avatarFile != null && !avatarFile.isEmpty()) {
				try {
					String fileName = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
					String uploadDir = servletContext.getRealPath("/images/avatars/");
					
					File dir = new File(uploadDir);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					
					Path path = Paths.get(uploadDir + File.separator + fileName);
					Files.copy(avatarFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					
					account.setAvatar(fileName);
				} catch (Exception e) {
					e.printStackTrace();
					redirectAttributes.addFlashAttribute("error", "Lỗi khi tải ảnh lên!");
					return "redirect:/account/profile";
				}
			}
			
			accountRepository.save(account);
			
			// Update session
			SessionUtils.set("user", account);
			redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
		} else {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản!");
		}

		return "redirect:/account/profile";
	}
}
