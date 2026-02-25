package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.data.domain.PageRequest;
import com.techone.repository.*;
import com.techone.model.*;
import java.util.*;

@Controller
public class ProductDetailController {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private VariantRepository variantRepository;

	@Autowired
	private SpecificationRepository specificationRepository;

	@Autowired
	private VariantAttributeValueRepository variantAttributeValueRepository;

	@GetMapping("/product/product-detail/{slug}")
	public String showProductDetail(@PathVariable("slug") String slug, Model model) {
		Product product = productRepository.findBySlug(slug).orElse(null);
		if (product == null) {
			return "redirect:/";
		}

		List<Variant> variants = variantRepository.findByProduct(product);
		List<Specification> specifications = specificationRepository.findByProduct(product);

		// Group attributes for the selection UI: Map<AttributeName, Set<Value>>
		Map<String, Set<String>> attributesMap = new LinkedHashMap<>();
		Map<Integer, String> variantAttributesMap = new HashMap<>();

		for (Variant v : variants) {
			List<VariantAttributeValue> vavs = variantAttributeValueRepository.findByVariant(v);
			List<String> attrValues = new ArrayList<>();
			for (VariantAttributeValue vav : vavs) {
				String attrName = vav.getAttributeValue().getAttribute().getName().trim();
				String attrValue = vav.getAttributeValue().getValue().trim();
				attributesMap.computeIfAbsent(attrName, k -> new LinkedHashSet<>()).add(attrValue);
				attrValues.add(attrValue);
			}
			Collections.sort(attrValues);
			variantAttributesMap.put(v.getId(), String.join("-", attrValues));
		}

		// Fetch similar products
		List<Product> similarProducts = new ArrayList<>();
		if (product.getCategory() != null) {
			Category category = product.getCategory();
			if (category.getParent() != null) {
				similarProducts = productRepository.findSimilarByParentCategory(
						category.getParent().getId(), product.getId(), PageRequest.of(0, 4));
			} else {
				similarProducts = productRepository.findSimilarByCategory(
						category.getId(), product.getId(), PageRequest.of(0, 4));
			}
		}

		model.addAttribute("product", product);
		model.addAttribute("variants", variants);
		model.addAttribute("specifications", specifications);
		model.addAttribute("attributesMap", attributesMap);
		model.addAttribute("variantAttributesMap", variantAttributesMap);
		model.addAttribute("similarProducts", similarProducts);

		return "views/client/product-detail";
	}
}
