package com.techone.controller.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.techone.model.Account;
import com.techone.model.Order;
import com.techone.repository.OrderRepository;
import com.techone.utils.SessionUtils;

@Controller
public class OrdersController {

	@Autowired
	private OrderRepository orderRepository;

	@GetMapping("/account/orders")
	public String showOders(Model model) {
		Account user = SessionUtils.get("user");
		if (user == null) {
			return "redirect:/login";
		}

		List<Order> orders = orderRepository.findByAccountIdOrderByCreateAtDesc(user.getId());
		model.addAttribute("orders", orders);

		return "views/client/orders";
	}
}
