package com.techone.controller.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.techone.model.Account;
import com.techone.model.Address;
import com.techone.model.Favourite;
import com.techone.model.Order;
import com.techone.repository.AddressRepository;
import com.techone.repository.FavouriteRepository;
import com.techone.repository.OrderRepository;
import com.techone.repository.VoucherItemRepository;
import com.techone.utils.SessionUtils;

@Controller
public class AccountController {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private VoucherItemRepository voucherItemRepository;

	@Autowired
	private FavouriteRepository favouriteRepository;

	@Autowired
	private AddressRepository addressRepository;

	@GetMapping("/account")
	public String showAccount(Model model) {
		Account user = SessionUtils.get("user");
		if (user == null) {
			return "redirect:/login";
		}

		Integer userId = user.getId();

		// Total orders
		long orderCount = orderRepository.countByAccountId(userId);
		model.addAttribute("orderCount", orderCount);

		// Unused vouchers
		long voucherCount = voucherItemRepository.countByAccountIdAndStatus(userId, 0);
		model.addAttribute("voucherCount", voucherCount);

		// Recent orders (latest 5)
		List<Order> allOrders = orderRepository.findByAccountIdOrderByCreateAtDesc(userId);
		List<Order> recentOrders = allOrders.stream().limit(5).toList();
		model.addAttribute("recentOrders", recentOrders);

		// Favourite products
		List<Favourite> favouriteProducts = favouriteRepository.findByAccount(user);
		model.addAttribute("favouriteProducts", favouriteProducts);

		// Default address
		Address defaultAddress = addressRepository.findByAccountIdAndStatusTrue(userId).orElse(null);
		model.addAttribute("defaultAddress", defaultAddress);

		return "views/client/account";
	}

}
