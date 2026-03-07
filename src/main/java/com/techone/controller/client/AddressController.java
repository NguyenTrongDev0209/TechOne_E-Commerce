package com.techone.controller.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.techone.model.Account;
import com.techone.model.Address;
import com.techone.model.Ward;
import com.techone.repository.WardRepository;
import com.techone.service.AddressService;
import com.techone.utils.SessionUtils;

@Controller
public class AddressController {

	@Autowired
	private AddressService addressService;

	@Autowired
	private WardRepository wardRepository;

	@GetMapping("/account/address")
	public String showAddress(Model model) {
		Account account = SessionUtils.get("user");
		if (account == null) {
			return "redirect:/login";
		}

		List<Address> addresses = addressService.findAddressesByAccount(account);
		model.addAttribute("addresses", addresses);
		model.addAttribute("address", new Address()); // For the form
		return "views/client/address";
	}

	@PostMapping("/account/address/add")
	public String addAddress(@jakarta.validation.Valid @ModelAttribute("address") Address address,
			BindingResult result,
			@RequestParam(value = "wardCode", required = false) String wardCode,
			@RequestParam(value = "isEdit", required = false, defaultValue = "false") boolean isEdit,
			Model model,
			RedirectAttributes redirectAttributes) {
		Account account = SessionUtils.get("user");
		if (account == null) {
			return "redirect:/login";
		}

		if (result.hasErrors() || wardCode == null || wardCode.isEmpty()) {
			List<Address> addresses = addressService.findAddressesByAccount(account);
			model.addAttribute("addresses", addresses);
			model.addAttribute("openModal", true);
			model.addAttribute("isEditMode", isEdit);

			if (wardCode != null && !wardCode.isEmpty()) {
				wardRepository.findById(wardCode).ifPresent(w -> {
					model.addAttribute("selectedProvinceId", w.getDistrict().getProvince().getId());
					model.addAttribute("selectedDistrictId", w.getDistrict().getId());
					model.addAttribute("selectedWardId", w.getId());
					model.addAttribute("selectedProvinceName", w.getDistrict().getProvince().getName());
					model.addAttribute("selectedDistrictName", w.getDistrict().getName());
					model.addAttribute("selectedWardName", w.getName());
				});
			}

			if (wardCode == null || wardCode.isEmpty()) {
				model.addAttribute("wardError", "Vui lòng chọn đầy đủ địa chỉ hành chính");
			}
			return "views/client/address";
		}

		Ward ward = wardRepository.findById(wardCode).orElse(null);
		if (ward == null) {
			List<Address> addresses = addressService.findAddressesByAccount(account);
			model.addAttribute("addresses", addresses);
			model.addAttribute("openModal", true);
			model.addAttribute("isEditMode", isEdit);
			model.addAttribute("wardError", "Phường/Xã không hợp lệ");
			return "views/client/address";
		}

		address.setWard(ward);
		addressService.save(address, account);

		redirectAttributes.addFlashAttribute("message", "Thao tác thành công!");
		return "redirect:/account/address";
	}

	@PostMapping("/account/address/update")
	public String updateAddress(@jakarta.validation.Valid @ModelAttribute("address") Address address,
			BindingResult result,
			@RequestParam(value = "wardCode", required = false) String wardCode,
			Model model,
			RedirectAttributes redirectAttributes) {
		return addAddress(address, result, wardCode, true, model, redirectAttributes);
	}

	@PostMapping("/account/address/delete")
	public String deleteAddress(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes) {
		Account account = SessionUtils.get("user");
		if (account == null) {
			return "redirect:/login";
		}

		addressService.delete(id, account);
		redirectAttributes.addFlashAttribute("message", "Đã xóa địa chỉ thành công!");
		return "redirect:/account/address";
	}
}
