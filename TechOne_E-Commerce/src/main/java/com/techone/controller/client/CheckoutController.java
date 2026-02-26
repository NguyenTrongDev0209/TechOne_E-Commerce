package com.techone.controller.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.model.Account;
import com.techone.model.Address;
import com.techone.model.Cart;
import com.techone.model.CartItem;
import com.techone.repository.AddressRepository;
import com.techone.repository.CartItemRepository;
import com.techone.repository.CartRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private AddressRepository addressRepository;

	@GetMapping("/checkout")
	public String showCheckout(@RequestParam(value = "itemIds", required = false) List<Integer> itemIds,
			HttpSession session, Model model) {
		Account user = (Account) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		// Get Address Data
		List<Address> addresses = addressRepository.findByAccountIdOrderByStatusDesc(user.getId());
		model.addAttribute("addresses", addresses);

		// Get default address setup
		Address defaultAddress = addresses.stream()
				.filter(Address::getStatus)
				.findFirst()
				.orElse(addresses.isEmpty() ? null : addresses.get(0));
		model.addAttribute("defaultAddress", defaultAddress);

		// Get Cart Data
		List<CartItem> cartItems = new ArrayList<>();
		Optional<Cart> cartOpt = cartRepository.findByAccountId(user.getId());
		if (cartOpt.isPresent()) {
			cartItems = cartItemRepository.findByCart(cartOpt.get());
			if (itemIds != null && !itemIds.isEmpty()) {
				cartItems = cartItems.stream()
						.filter(item -> itemIds.contains(item.getId()))
						.toList();
			}
		}

		if (cartItems.isEmpty()) {
			return "redirect:/cart";
		}

		model.addAttribute("cartItems", cartItems);

		return "views/client/checkout";
	}

}
