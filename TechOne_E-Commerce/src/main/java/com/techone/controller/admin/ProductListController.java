package com.techone.controller.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.model.Product;
import com.techone.repository.BrandRepository;
import com.techone.repository.CategoryRepository;
import com.techone.repository.ProductRepository;

@Controller
public class ProductListController {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	BrandRepository brandRepository;

	@GetMapping("/admin/product-list")
	public String listProducts(Model model,
			@RequestParam("pageNum") Optional<Integer> pageNum,
			@RequestParam("pageSize") Optional<Integer> pageSize,
			@RequestParam("keyword") Optional<String> keyword,
			@RequestParam("categoryId") Optional<Integer> categoryId,
			@RequestParam("brandId") Optional<Integer> brandId,
			@RequestParam("status") Optional<Integer> status,
			@RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> fromDate,
			@RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> toDate) {

		int pageNumber = pageNum.orElse(0);
		int pageSizes = pageSize.orElse(10);
		String searchKeyword = keyword.orElse(null);
		Integer searchCategoryId = categoryId.orElse(null);
		Integer searchBrandId = brandId.orElse(null);
		Integer searchStatus = status.orElse(null);

		LocalDateTime startDateTime = fromDate.isPresent() ? fromDate.get().atStartOfDay() : null;
		LocalDateTime endDateTime = toDate.isPresent() ? toDate.get().atTime(LocalTime.MAX) : null;

		Pageable pageable = PageRequest.of(pageNumber, pageSizes, Direction.DESC, "id");

		Page<Product> page = productRepository.search(
				searchKeyword,
				searchCategoryId,
				searchBrandId,
				searchStatus,
				startDateTime,
				endDateTime,
				pageable);

		model.addAttribute("list", page.getContent());
		model.addAttribute("page", page);
		model.addAttribute("categories", categoryRepository.findByType(true)); // Product Categories
		model.addAttribute("brands", brandRepository.findAll());
		model.addAttribute("active", "products");

		// Preserve search criteria in UI
		model.addAttribute("keyword", searchKeyword);
		model.addAttribute("categoryId", searchCategoryId);
		model.addAttribute("brandId", searchBrandId);
		model.addAttribute("status", searchStatus);
		model.addAttribute("fromDate", fromDate.orElse(null));
		model.addAttribute("toDate", toDate.orElse(null));

		return "views/admin/product-list";
	}

	@GetMapping("/admin/product/toggle-status/{id}")
	public String toggleStatus(@PathVariable("id") Integer id) {
		Product product = productRepository.findById(id).orElse(null);
		if (product != null) {
			product.setStatus(product.getStatus() == 1 ? 0 : 1);
			productRepository.save(product);
			return "redirect:/admin/product-list?updated=true";
		}
		return "redirect:/admin/product-list?error=ProductNotFound";
	}

	@GetMapping("/admin/product/delete/{id}")
	public String deleteProduct(@PathVariable("id") Integer id) {
		Product product = productRepository.findById(id).orElse(null);
		if (product != null) {
			// Check for constraints (orders, etc.) - simple delete for now or soft delete
			try {
				productRepository.deleteById(id);
				return "redirect:/admin/product-list?deleted=true";
			} catch (Exception e) {
				return "redirect:/admin/product-list?error=DeleteFailed";
			}
		}
		return "redirect:/admin/product-list?error=ProductNotFound";
	}

}
