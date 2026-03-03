package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.model.Account;
import com.techone.model.Order;
import com.techone.repository.OrderRepository;
import com.techone.utils.SessionUtils;

@Controller
public class OderDetailController {

	@Autowired
	private OrderRepository orderRepository;

	@GetMapping("/account/orders/order-detail")
	public String orderDetail(@RequestParam("id") Integer id, Model model) {
		Account user = SessionUtils.get("user");
		if (user == null) {
			return "redirect:/login";
		}

		Order order = orderRepository.findById(id).orElse(null);

		// Security check: ensure the order belongs to the logged-in user
		if (order == null || !order.getAccount().getId().equals(user.getId())) {
			return "redirect:/account/orders";
		}

		model.addAttribute("order", order);
		return "views/client/order-detail";
	}
}
