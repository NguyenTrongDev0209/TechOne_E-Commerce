package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.model.Product;
import com.techone.repository.ProductRepository;

@Controller
public class SearchController {

	@Autowired
	private ProductRepository productRepository;

	@GetMapping("/search")
	public String showSearch(@RequestParam(value = "keywords", required = false) String keywords,
			@RequestParam(value = "page", defaultValue = "0") int page,
			Model model) {
		
		Page<Product> productPage;
		if (keywords != null && !keywords.trim().isEmpty()) {
			productPage = productRepository.search(keywords, null, null, true, null, null, PageRequest.of(page, 12));
		} else {
			productPage = productRepository.search(null, null, null, true, null, null, PageRequest.of(page, 12));
		}

		model.addAttribute("productPage", productPage);
		model.addAttribute("keywords", keywords);
		model.addAttribute("totalItems", productPage.getTotalElements());

		return "views/client/search";
	}

}
