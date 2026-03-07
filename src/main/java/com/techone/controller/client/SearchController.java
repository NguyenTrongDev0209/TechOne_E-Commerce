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

		org.springframework.data.domain.Pageable pageable = PageRequest.of(page, 12,
				org.springframework.data.domain.Sort.by("id").descending());
		Page<Product> productPage = productRepository.search(keywords, null, null, true, null, null, null, null,
				pageable);

		model.addAttribute("productPage", productPage);
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("keywords", keywords);
		model.addAttribute("totalItems", productPage.getTotalElements());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());

		return "views/client/search";
	}

}
