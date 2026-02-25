package com.techone.controller.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.techone.model.Account;
import com.techone.model.Cart;
import com.techone.model.CartItem;
import com.techone.repository.CartItemRepository;
import com.techone.repository.CartRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@GetMapping("/cart")
	public String showCart(HttpSession session, Model model) {
		Account user = (Account) session.getAttribute("user");
		List<CartItem> cartItems = new ArrayList<>();
		if (user != null) {
			Optional<Cart> cartOpt = cartRepository.findByAccountId(user.getId());
			if (cartOpt.isPresent()) {
				cartItems = cartItemRepository.findByCart(cartOpt.get());
			}
			session.setAttribute("cartCount", cartItems.size());
		}
		model.addAttribute("cartItems", cartItems);
		return "views/client/cart";
	}
}
