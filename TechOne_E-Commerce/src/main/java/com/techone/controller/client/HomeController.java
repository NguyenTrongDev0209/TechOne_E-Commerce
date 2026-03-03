package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.techone.repository.BrandRepository;
import com.techone.repository.CategoryRepository;
import com.techone.repository.PostRepository;
import com.techone.repository.ProductRepository;
import com.techone.service.CookieService;
import com.techone.model.Product;
import com.techone.dto.BrandCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private CookieService cookieService;

	@Autowired
	private PostRepository postRepository;

	@GetMapping("/")
	public String showHome(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "0") int viewedPage,
			@RequestParam(defaultValue = "0") int bestSellerPage,
			Model model) {
		// Lấy danh mục cha có type = true (sản phẩm) và status = true
		model.addAttribute("categories", categoryRepository.findByTypeAndParentIsNullAndStatus(true, true));

		// Best Sellers section (3 items per page)
		Pageable bestSellerPageable = PageRequest.of(bestSellerPage, 3);
		Page<Product> bestSellerProductPage = productRepository.findBestSellers(bestSellerPageable);
		model.addAttribute("bestSellers", bestSellerProductPage.getContent());
		model.addAttribute("bestSellerCurrentPage", bestSellerPage);
		model.addAttribute("bestSellerTotalPages", bestSellerProductPage.getTotalPages());

		// Lấy danh sách sản phẩm có status = true với phân trang (5 sản phẩm mỗi trang)
		Pageable pageable = PageRequest.of(page, 5, Sort.by("id").descending());
		Page<Product> productPage = productRepository.findByStatus(true, pageable);

		model.addAttribute("products", productPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());

		// Lấy danh sách thương hiệu có status = true và có ít nhất 1 sản phẩm
		model.addAttribute("brands", brandRepository.findActiveBrandsWithProductCount());

		// Tin công nghệ mới: 3 bài viết mới nhất
		model.addAttribute("latestPosts", postRepository.findTop3ByStatusOrderByCreateAtDesc(true));

		// Tin tức & Sự kiện: 5 bài viết nhiều lượt xem nhất
		model.addAttribute("topViewedPosts", postRepository.findTop5ByStatusOrderByViewCountDesc(true));

		// viewed products from cookie
		String viewedIds = cookieService.getValue("viewed_products");
		if (!viewedIds.isEmpty()) {
			try {
				List<Integer> ids = Arrays.stream(viewedIds.split("-"))
						.filter(s -> !s.isEmpty())
						.map(Integer::parseInt)
						.collect(Collectors.toList());

				Pageable viewedPageable = PageRequest.of(viewedPage, 5);
				Page<Product> viewedProductPage = productRepository.findByIdInAndStatus(ids, viewedPageable);

				// Re-sort to match the order in the cookie (most recent first)
				List<Product> viewedProductsOrdered = new ArrayList<>();
				List<Product> fetchedProducts = viewedProductPage.getContent();
				for (Integer id : ids) {
					fetchedProducts.stream()
							.filter(p -> p.getId().equals(id))
							.findFirst()
							.ifPresent(viewedProductsOrdered::add);
				}

				model.addAttribute("viewedProducts", viewedProductsOrdered);
				model.addAttribute("viewedCurrentPage", viewedPage);
				model.addAttribute("viewedTotalPages", viewedProductPage.getTotalPages());
			} catch (Exception e) {
				// Handle invalid format or data in cookie
				cookieService.remove("viewed_products");
			}
		}

		return "views/client/home";
	}
}
