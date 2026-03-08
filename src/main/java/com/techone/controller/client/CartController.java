package com.techone.controller.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.techone.domain.user.entity.Account;
import com.techone.domain.order.entity.Cart;
import com.techone.domain.order.entity.CartItem;
import com.techone.domain.order.repository.CartItemRepository;
import com.techone.domain.order.repository.CartRepository;
import com.techone.domain.order.repository.OrderRepository;
import com.techone.domain.order.entity.Order;
import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private OrderRepository orderRepository;

	@GetMapping("/cart")
	public String showCart(HttpSession session, Model model) {
		Account user = (Account) session.getAttribute("user");
		List<CartItem> cartItems = new ArrayList<>();

		if (user != null) {
			// Check for pending order (Status = 1)
			Optional<Order> pendingOrder = orderRepository.findFirstByAccountIdAndStatus(user.getId(), 1);
			if (pendingOrder.isPresent()) {
				return "redirect:/checkout/payos?orderId=" + pendingOrder.get().getId();
			}

			Optional<Cart> cartOpt = cartRepository.findByAccountId(user.getId());
			if (cartOpt.isPresent()) {
				cartItems = cartItemRepository.findActiveItemsByCart(cartOpt.get());
			}
			session.setAttribute("cartCount", cartItems.size());
		}
		model.addAttribute("cartItems", cartItems);
		return "views/client/cart";
	}
}


