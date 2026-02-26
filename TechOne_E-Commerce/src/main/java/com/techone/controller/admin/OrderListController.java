package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.techone.model.Order;
import com.techone.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderListController {

	private final OrderRepository orderRepository;

	@GetMapping("/admin/order-list")
	public String orderList(Model model) {
		List<Order> orders = orderRepository.findAllByOrderByCreateAtDesc();
		model.addAttribute("orders", orders);
		return "views/admin/order-list";
	}

}
