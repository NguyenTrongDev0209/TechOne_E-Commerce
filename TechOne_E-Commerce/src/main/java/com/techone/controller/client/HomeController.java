package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.techone.repository.BrandRepository;
import com.techone.repository.CategoryRepository;
import com.techone.repository.ProductRepository;

@Controller
public class HomeController {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private BrandRepository brandRepository;

	@GetMapping("/")
	public String showHome(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
			Model model) {
		// Lấy danh mục cha có type = true (sản phẩm) và status = true
		model.addAttribute("categories", categoryRepository.findByTypeAndParentIsNullAndStatus(true, true));

		// Lấy danh sách sản phẩm có status = true với phân trang (10 sản phẩm mỗi
		// trang)
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 10,
				org.springframework.data.domain.Sort.by("id").descending());
		org.springframework.data.domain.Page<com.techone.model.Product> productPage = productRepository
				.findByStatus(true, pageable);

		model.addAttribute("products", productPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());

		// Lấy danh sách thương hiệu có status = true
		model.addAttribute("brands", brandRepository.findByStatus(true));

		return "views/client/home";
	}

}
