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
import com.techone.model.Category;
import com.techone.model.Variant;
import com.techone.model.Specification;
import com.techone.repository.BrandRepository;
import com.techone.repository.CategoryRepository;
import com.techone.repository.ProductRepository;
import com.techone.repository.VariantRepository;
import com.techone.repository.SpecificationRepository;
import com.techone.repository.ImageProductRepository;
import com.techone.repository.VariantAttributeValueRepository;
import com.techone.repository.VariantImageRepository;
import com.techone.repository.SpecificationTitleRepository;
import com.techone.repository.SpecificationValueRepository;
import com.techone.repository.CartItemRepository;
import com.techone.repository.FavouriteRepository;
import com.techone.repository.OrderDetailRepository;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class ProductListController {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	BrandRepository brandRepository;

	@Autowired
	VariantRepository variantRepository;

	@Autowired
	SpecificationRepository specificationRepository;

	@Autowired
	ImageProductRepository imageProductRepository;

	@Autowired
	VariantAttributeValueRepository variantAttributeValueRepository;

	@Autowired
	VariantImageRepository variantImageRepository;

	@Autowired
	SpecificationTitleRepository specificationTitleRepository;

	@Autowired
	SpecificationValueRepository specificationValueRepository;

	@Autowired
	CartItemRepository cartItemRepository;

	@Autowired
	FavouriteRepository favouriteRepository;

	@Autowired
	OrderDetailRepository orderDetailRepository;

	@GetMapping("/admin/product-list")
	public String listProducts(Model model,
			@RequestParam("pageNum") Optional<Integer> pageNum,
			@RequestParam("pageSize") Optional<Integer> pageSize,
			@RequestParam("keyword") Optional<String> keyword,
			@RequestParam("categoryId") Optional<Integer> categoryId,
			@RequestParam("brandId") Optional<Integer> brandId,
			@RequestParam("status") Optional<Boolean> status,
			@RequestParam("fromDate") @DateTimeFormat(pattern = "dd/MM/yyyy") Optional<LocalDate> fromDate,
			@RequestParam("toDate") @DateTimeFormat(pattern = "dd/MM/yyyy") Optional<LocalDate> toDate) {

		int pageNumber = pageNum.orElse(0);
		int pageSizes = pageSize.orElse(10);
		String searchKeyword = keyword.orElse(null);
		Integer searchCategoryId = categoryId.orElse(null);
		Integer searchBrandId = brandId.orElse(null);
		Boolean searchStatus = status.orElse(null);

		LocalDateTime startDateTime = fromDate.isPresent() ? fromDate.get().atStartOfDay() : null;
		LocalDateTime endDateTime = toDate.isPresent() ? toDate.get().atTime(LocalTime.MAX) : null;

		Pageable pageable = PageRequest.of(pageNumber, pageSizes, Direction.DESC, "id");

		Page<Product> page = productRepository.search(
				searchKeyword,
				searchCategoryId,
				searchBrandId,
				searchStatus,
				null, // minPrice
				null, // maxPrice
				startDateTime,
				endDateTime,
				pageable);

		model.addAttribute("list", page.getContent());
		model.addAttribute("page", page);
		model.addAttribute("categories", categoryRepository.findByTypeAndStatusAndParentActive(true, true)); // Active
																												// Product
		// Categories
		model.addAttribute("parentCategories", categoryRepository.findByTypeAndParentIsNullAndStatus(true, true)); // Active
																													// Parent
																													// Categories
		model.addAttribute("brands", brandRepository.findAll());
		model.addAttribute("active", "products");

		// Preserve search criteria in UI
		model.addAttribute("keyword", searchKeyword);
		model.addAttribute("categoryId", searchCategoryId);
		model.addAttribute("brandId", searchBrandId);
		model.addAttribute("status", searchStatus);

		// Resolve selected category name for UI label
		String catName = "Tất cả danh mục";
		if (searchCategoryId != null) {
			Optional<Category> cat = categoryRepository.findById(searchCategoryId);
			if (cat.isPresent()) {
				catName = cat.get().getName();
			}
		}
		model.addAttribute("categoryName", catName);
		model.addAttribute("status", searchStatus);

		return "views/admin/product-list";
	}

	@GetMapping("/admin/product/toggle-status/{id}")
	public String toggleStatus(@PathVariable("id") Integer id) {
		Product product = productRepository.findById(id).orElse(null);
		if (product != null) {
			boolean currentStatus = product.getStatus() != null && product.getStatus();
			boolean nextStatus = !currentStatus;

			if (nextStatus) { // Trying to activate
				if (product.getCategory() != null) {
					Category cat = product.getCategory();
					boolean isCategoryHidden = Boolean.FALSE.equals(cat.getStatus())
							|| (cat.getParent() != null && Boolean.FALSE.equals(cat.getParent().getStatus()));
					if (isCategoryHidden) {
						return "redirect:/admin/product-list?error=CategoryIsHidden";
					}
				}
				if (product.getBrand() != null && Boolean.FALSE.equals(product.getBrand().getStatus())) {
					return "redirect:/admin/product-list?error=BrandIsHidden";
				}
			}

			product.setStatus(nextStatus);
			productRepository.save(product);
			return "redirect:/admin/product-list?updated=true";
		}
		return "redirect:/admin/product-list?error=ProductNotFound";
	}

	@Transactional
	@GetMapping("/admin/product/delete/{id}")
	public String deleteProduct(@PathVariable("id") Integer id) {
		Product product = productRepository.findById(id).orElse(null);
		if (product != null) {
			try {
				// Delete Image Products
				if (product.getImageProduct() != null) {
					imageProductRepository.deleteAll(product.getImageProduct());
				}

				// Delete Specifications
				java.util.List<Specification> specs = specificationRepository.findByProduct(product);
				if (specs != null) {
					for (Specification spec : specs) {
						if (spec.getSpecificationTitles() != null) {
							for (com.techone.model.SpecificationTitle title : spec.getSpecificationTitles()) {
								if (title.getSpecificationValues() != null) {
									specificationValueRepository.deleteAll(title.getSpecificationValues());
								}
								specificationTitleRepository.delete(title);
							}
						}
						specificationRepository.delete(spec);
					}
				}

				// Delete Variants
				if (product.getVariant() != null) {
					for (Variant v : product.getVariant()) {
						// CRITICAL: Check if variant is in any orders
						if (!orderDetailRepository.findByVariant(v).isEmpty()) {
							return "redirect:/admin/product-list?error=ProductInOrder";
						}

						// Delete dependent data
						java.util.List<com.techone.model.VariantAttributeValue> vavs = variantAttributeValueRepository
								.findByVariant(v);
						if (vavs != null)
							variantAttributeValueRepository.deleteAll(vavs);

						java.util.List<com.techone.model.VariantImage> vImages = variantImageRepository
								.findByVariant(v);
						if (vImages != null)
							variantImageRepository.deleteAll(vImages);

						// Clean up favorites and cart items
						favouriteRepository.deleteByVariant(v);
						cartItemRepository.deleteByVariant(v);

						variantRepository.delete(v);
					}
				}

				productRepository.deleteById(id);
				return "redirect:/admin/product-list?deleted=true";
			} catch (Exception e) {
				e.printStackTrace(); // Log the error for debugging
				return "redirect:/admin/product-list?error=DeleteFailed";
			}
		}
		return "redirect:/admin/product-list?error=ProductNotFound";
	}

}
