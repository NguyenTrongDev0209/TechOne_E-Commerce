package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.domain.product.entity.Category;
import com.techone.domain.product.entity.Product;
import com.techone.domain.product.repository.CategoryRepository;
import com.techone.domain.product.repository.ProductRepository;

@Controller
public class CategoryController {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@GetMapping({ "/categories", "/categories/{slug}" })
	public String showCategories(
			@PathVariable(required = false) String slug,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(required = false) Integer brandId,
			@RequestParam(required = false) Double minPrice,
			@RequestParam(required = false) Double maxPrice,
			Model model) {

		Pageable pageable = PageRequest.of(page, 4, Sort.by("id").descending());
		Page<Product> productPage;

		Integer categoryId = null;
		if (slug != null) {
			Category category = categoryRepository.findBySlug(slug);
			if (category != null) {
				model.addAttribute("currentCategory", category);
				categoryId = category.getId();
			}
		}

		productPage = productRepository.search(null, categoryId, brandId, true, minPrice, maxPrice, null, null,
				pageable);

		model.addAttribute("productPage", productPage);
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("currentSlug", slug);

		// Pass filter values back to UI
		model.addAttribute("selectedBrandId", brandId);
		model.addAttribute("minPrice", minPrice);
		model.addAttribute("maxPrice", maxPrice);

		return "views/client/categories";
	}
}

