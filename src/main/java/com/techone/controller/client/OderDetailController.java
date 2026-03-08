package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.domain.user.entity.Account;
import com.techone.domain.order.entity.Order;
import com.techone.domain.order.repository.OrderRepository;
import com.techone.common.utils.SessionUtils;

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

	@org.springframework.web.bind.annotation.PostMapping("/account/orders/confirm-delivery")
	public String confirmDelivery(@RequestParam("id") Integer id, @RequestParam("status") Integer status) {
		Account user = SessionUtils.get("user");
		if (user == null) {
			return "redirect:/login";
		}

		Order order = orderRepository.findById(id).orElse(null);
		if (order != null && order.getAccount().getId().equals(user.getId()) && order.getStatus() == 5) {
			// Status 6: Received, 7: Not Received
			if (status == 6 || status == 7) {
				order.setStatus(status);
				orderRepository.save(order);
			}
		}
		return "redirect:/account/orders/order-detail?id=" + id;
	}
}


