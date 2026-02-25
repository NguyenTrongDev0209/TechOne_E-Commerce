package com.techone.controller.admin;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.techone.model.Brand;
import com.techone.model.Category;
import com.techone.model.Product;
import com.techone.model.Account;
import com.techone.utils.SessionUtils;
import com.techone.repository.BrandRepository;
import com.techone.repository.CategoryRepository;
import com.techone.repository.ProductRepository;
import com.techone.utils.SlugUtils;
import com.techone.model.Variant;
import com.techone.model.Attribute;
import com.techone.model.AttributeValue;
import com.techone.model.VariantAttributeValue;
import com.techone.model.VariantImage;
import com.techone.repository.AttributeRepository;
import com.techone.repository.AttributeValueRepository;
import com.techone.repository.VariantRepository;
import com.techone.repository.VariantAttributeValueRepository;
import com.techone.repository.VariantImageRepository;
import com.techone.dto.VariantPayloadDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.techone.utils.FileUploadUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

@Controller
public class ProductFormController {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	BrandRepository brandRepository;

	@Autowired
	com.techone.repository.ImageProductRepository imageProductRepository;

	@Autowired
	AttributeRepository attributeRepository;

	@Autowired
	AttributeValueRepository attributeValueRepository;

	@Autowired
	VariantRepository variantRepository;

	@Autowired
	VariantAttributeValueRepository variantAttributeValueRepository;

	@Autowired
	VariantImageRepository variantImageRepository;

	@Autowired
	com.techone.repository.SpecificationRepository specificationRepository;

	@Autowired
	com.techone.repository.SpecificationTitleRepository specificationTitleRepository;

	@Autowired
	com.techone.repository.SpecificationValueRepository specificationValueRepository;

	@Autowired
	FileUploadUtils fileUploadUtils;

	@Autowired
	Validator validator;

	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	@GetMapping("/admin/product-list/product-form")
	public String showForm(Model model) {
		Product product = new Product();
		product.setStatus(true); // Default status Active
		product.setCreateAt(LocalDateTime.now());
		product.setCategory(new Category());
		product.setBrand(new Brand());
		model.addAttribute("product", product);
		model.addAttribute("editMode", false);
		loadFormAttributes(model);
		model.addAttribute("active", "products");
		return "views/admin/product-form";
	}

	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	@GetMapping("/admin/product-list/product-form/{slug}")
	public String editProduct(Model model, @PathVariable("slug") String slug) {
		Product product = productRepository.findBySlug(slug).orElse(null);
		if (product == null) {
			try {
				Integer id = Integer.parseInt(slug);
				product = productRepository.findById(id).orElse(null);
			} catch (NumberFormatException e) {
				// Not an ID, keep product as null
			}
		}

		if (product == null) {
			return "redirect:/admin/product-list?error=ProductNotFound";
		}
		if (product.getCategory() == null)
			product.setCategory(new Category());
		if (product.getBrand() == null)
			product.setBrand(new Brand());

		// Convert existing variants and specifications back to JSON for the frontend
		try {
			ObjectMapper mapper = new ObjectMapper();

			// Transform Variants
			if (product.getVariant() != null && !product.getVariant().isEmpty()) {
				java.util.List<VariantPayloadDto> vList = new java.util.ArrayList<>();
				for (Variant v : product.getVariant()) {
					VariantPayloadDto dto = new VariantPayloadDto();
					dto.setSku(v.getSku());
					dto.setPrice(v.getPrice());
					dto.setDiscount(v.getDiscount());
					dto.setStock(v.getStock());

					java.util.Map<String, String> attrs = new java.util.HashMap<>();
					java.util.List<VariantAttributeValue> vvs = variantAttributeValueRepository.findByVariant(v);
					if (vvs != null) {
						for (VariantAttributeValue vav : vvs) {
							if (vav.getAttributeValue() != null && vav.getAttributeValue().getAttribute() != null) {
								attrs.put(vav.getAttributeValue().getAttribute().getName(),
										vav.getAttributeValue().getValue());
							}
						}
					}
					// If no attributes, it's a default variant
					if (attrs.isEmpty()) {
						attrs.put("Mặc định", "Mặc định");
					}
					java.util.List<VariantImage> vImages = variantImageRepository.findByVariant(v);
					if (vImages != null && !vImages.isEmpty()) {
						dto.setExistingImages(vImages.stream().map(VariantImage::getPathImage)
								.collect(java.util.stream.Collectors.toList()));
					}

					dto.setAttributes(attrs);
					vList.add(dto);
				}
				model.addAttribute("variantsJson", mapper.writeValueAsString(vList));
			}

			// Transform Specifications
			java.util.List<com.techone.model.Specification> specs = specificationRepository.findByProduct(product);
			if (specs != null && !specs.isEmpty()) {
				com.techone.model.Specification spec = specs.get(0);
				java.util.List<com.techone.dto.SpecificationPayloadDto> sList = new java.util.ArrayList<>();
				if (spec.getSpecificationTitles() != null) {
					for (com.techone.model.SpecificationTitle title : spec.getSpecificationTitles()) {
						com.techone.dto.SpecificationPayloadDto gDto = new com.techone.dto.SpecificationPayloadDto();
						gDto.setGroupName(title.getName());

						java.util.List<com.techone.dto.SpecificationItemDto> items = new java.util.ArrayList<>();
						if (title.getSpecificationValues() != null) {
							for (com.techone.model.SpecificationValue val : title.getSpecificationValues()) {
								com.techone.dto.SpecificationItemDto iDto = new com.techone.dto.SpecificationItemDto();
								String rawName = val.getName();
								if (rawName != null && rawName.contains(": ")) {
									String[] parts = rawName.split(": ", 2);
									iDto.setName(parts[0]);
									iDto.setValue(parts[1]);
								} else {
									iDto.setName(rawName);
									iDto.setValue("");
								}
								items.add(iDto);
							}
						}
						gDto.setItems(items);
						sList.add(gDto);
					}
				}
				model.addAttribute("specificationsJson", mapper.writeValueAsString(sList));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		model.addAttribute("product", product);
		model.addAttribute("editMode", true);
		model.addAttribute("viewMode", false);
		loadFormAttributes(model);
		model.addAttribute("active", "products");
		return "views/admin/product-form";
	}

	@org.springframework.transaction.annotation.Transactional
	@PostMapping("/admin/product-list/product-form/save")
	public String saveProduct(Model model,
			@ModelAttribute("product") @Valid Product product,
			Errors errors,
			@org.springframework.web.bind.annotation.RequestParam(value = "imageFiles", required = false) org.springframework.web.multipart.MultipartFile[] images,
			@org.springframework.web.bind.annotation.RequestParam(value = "variantsJson", required = false) String variantsJson,
			@org.springframework.web.bind.annotation.RequestParam(value = "specificationsJson", required = false) String specificationsJson,
			MultipartHttpServletRequest req) {

		// Validation for existing category/brand IDs (since they are nested objects)
		if (product.getCategory() == null || product.getCategory().getId() == null) {
			errors.rejectValue("category", "error.product", "Chưa chọn danh mục");
		}
		if (product.getBrand() == null || product.getBrand().getId() == null) {
			errors.rejectValue("brand", "error.product", "Chưa chọn thương hiệu");
		}

		// Validation for Category/Brand Status if product is Active
		if (Boolean.TRUE.equals(product.getStatus())) {
			if (product.getCategory() != null && product.getCategory().getId() != null) {
				Category cat = categoryRepository.findById(product.getCategory().getId()).orElse(null);
				if (cat != null && Boolean.FALSE.equals(cat.getStatus())) {
					errors.rejectValue("status", "error.product",
							"Không thể kích hoạt sản phẩm vì danh mục đang bị ẩn");
				}
			}
			if (product.getBrand() != null && product.getBrand().getId() != null) {
				Brand brand = brandRepository.findById(product.getBrand().getId()).orElse(null);
				if (brand != null && Boolean.FALSE.equals(brand.getStatus())) {
					errors.rejectValue("status", "error.product",
							"Không thể kích hoạt sản phẩm vì thương hiệu đang bị ẩn");
				}
			}
		}

		// Validation for images (at least one image if new product, or check size)
		boolean hasNewImages = false;
		if (images != null && images.length > 0) {
			long totalSize = 0;
			for (org.springframework.web.multipart.MultipartFile img : images) {
				if (!img.isEmpty()) {
					hasNewImages = true;
					totalSize += img.getSize();
				}
			}
			if (totalSize > 20 * 1024 * 1024) {
				model.addAttribute("imageError", "Hình ảnh không quá 20MB");
			}
		}

		// We bypass the strict product image validation for new products since variants
		// can provide their own images.
		// if (product.getId() == null && !hasNewImages) {
		// model.addAttribute("imageError", "Hình ảnh không được trống");
		// }

		// Handle Variants Validation manually since it's parsed from JSON
		if (variantsJson != null && !variantsJson.isEmpty() && !variantsJson.equals("[]")) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.enable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
				java.util.List<VariantPayloadDto> variantPayloads = mapper.readValue(variantsJson,
						new TypeReference<java.util.List<VariantPayloadDto>>() {
						});

				java.util.Map<Integer, java.util.Map<String, String>> fieldErrorsMap = new java.util.HashMap<>();
				StringBuilder variantErrors = new StringBuilder();
				int index = 0;
				for (VariantPayloadDto payload : variantPayloads) {
					java.util.Set<ConstraintViolation<VariantPayloadDto>> violations = validator.validate(payload);
					if (!violations.isEmpty()) {
						java.util.Map<String, String> fErrors = new java.util.HashMap<>();
						for (ConstraintViolation<VariantPayloadDto> violation : violations) {
							fErrors.put(violation.getPropertyPath().toString(), violation.getMessage());
							variantErrors.append("Biến thể ")
									.append(payload.getSku() != null ? payload.getSku() : "Mới").append(": ")
									.append(violation.getMessage()).append(".<br/>");
						}
						fieldErrorsMap.put(index, fErrors);
					}

					// Validate Attributes explicitly (No empty attribute name allowed)
					if (payload.getAttributes() != null) {
						for (String attrName : payload.getAttributes().keySet()) {
							if (attrName == null || attrName.trim().isEmpty()) {
								variantErrors.append("Tên thuộc tính không được trống.<br/>");
								break;
							}
						}
					}

					// Validate Variant Images (pathImage)
					boolean hasImage = false;
					if (payload.getImageInputName() != null) {
						org.springframework.web.multipart.MultipartFile[] variantImagesFiles = req
								.getFiles(payload.getImageInputName())
								.toArray(new org.springframework.web.multipart.MultipartFile[0]);
						for (org.springframework.web.multipart.MultipartFile f : variantImagesFiles) {
							if (!f.isEmpty()) {
								hasImage = true;
								break;
							}
						}
					}

					if (!hasImage) {
						java.util.Map<String, String> fMap = fieldErrorsMap.getOrDefault(index,
								new java.util.HashMap<>());
						fMap.put("pathImage", "Hình ảnh không được trống");
						fieldErrorsMap.put(index, fMap);
					}

					index++;
				}

				if (!fieldErrorsMap.isEmpty()) {
					model.addAttribute("variantFieldErrors", mapper.writeValueAsString(fieldErrorsMap));
				}

				if (variantErrors.length() > 0) {
					model.addAttribute("variantError", variantErrors.toString());
				}
			} catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
				e.printStackTrace();
				model.addAttribute("variantError",
						"Xin vui lòng điền đầy đủ số lượng và giá trong tất cả các biến thể.");
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("variantError", "Lỗi định dạng dữ liệu biến thể: Không thể xử lý dữ liệu.");
			}
		}

		if (errors.hasErrors() || model.containsAttribute("imageError") || model.containsAttribute("variantError")) {
			loadFormAttributes(model);
			model.addAttribute("active", "products");
			model.addAttribute("editMode", product.getId() != null);
			model.addAttribute("variantsJson", variantsJson);
			model.addAttribute("specificationsJson", specificationsJson);
			return "views/admin/product-form";
		}

		// Handle Slug
		if (product.getSlug() == null || product.getSlug().isEmpty()) {
			product.setSlug(SlugUtils.toSlug(product.getName()));
		}

		// Determine if this is an edit or new creation before saving the entity
		boolean isEdit = product.getId() != null;

		// Set CreateAt and Account if new
		if (!isEdit) {
			product.setCreateAt(LocalDateTime.now());
			Account currentUser = SessionUtils.get("user");
			if (currentUser != null) {
				product.setAccount(currentUser);
			}
		} else {
			Product existing = productRepository.findById(product.getId()).orElse(null);
			if (existing != null) {
				product.setCreateAt(existing.getCreateAt());
				product.setAccount(existing.getAccount());
			}
		}

		Product savedProduct = productRepository.save(product);

		// Handle Deleting old Variants and Specifications if Edit Mode
		if (isEdit) {
			// 1. Delete old Variants
			java.util.List<Variant> oldVariants = variantRepository.findByProduct(savedProduct);
			if (oldVariants != null) {
				for (Variant v : oldVariants) {
					// Delete associated attribute values
					java.util.List<VariantAttributeValue> vavs = variantAttributeValueRepository.findByVariant(v);
					if (vavs != null)
						variantAttributeValueRepository.deleteAll(vavs);

					// Delete associated images
					java.util.List<VariantImage> vImages = variantImageRepository.findByVariant(v);
					if (vImages != null)
						variantImageRepository.deleteAll(vImages);

					variantRepository.delete(v);
				}
			}

			// 2. Delete old Specifications
			java.util.List<com.techone.model.Specification> oldSpecs = specificationRepository
					.findByProduct(savedProduct);
			if (oldSpecs != null) {
				for (com.techone.model.Specification spec : oldSpecs) {
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
			// Flush to ensure deletions are committed before creating new ones (avoids SKU
			// conflicts)
			variantRepository.flush();
			specificationRepository.flush();
		}

		// Handle Images Saving
		if (hasNewImages) {
			for (org.springframework.web.multipart.MultipartFile image : images) {
				if (image != null && !image.isEmpty()) {
					try {
						String vFilename = fileUploadUtils.saveImage(image, "products");
						com.techone.model.ImageProduct imageProduct = new com.techone.model.ImageProduct();
						imageProduct.setUrl(vFilename);
						imageProduct.setProduct(savedProduct);
						imageProductRepository.save(imageProduct);
					} catch (java.io.IOException e) {
						throw new RuntimeException("Lỗi lưu file hình ảnh sản phẩm", e);
					}
				}
			}
		}

		// Handle Variants Dynamic Generation
		if (variantsJson != null && !variantsJson.isEmpty() && !variantsJson.equals("[]")) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				java.util.List<VariantPayloadDto> variantPayloads = mapper.readValue(variantsJson,
						new TypeReference<java.util.List<VariantPayloadDto>>() {
						});

				int totalStock = 0;

				for (VariantPayloadDto payload : variantPayloads) {
					int variantStock = payload.getStock() != null ? payload.getStock() : 0;
					totalStock += variantStock;

					// 1. Save Variant
					Variant variant = new Variant();
					variant.setProduct(savedProduct);
					variant.setSku(payload.getSku());
					variant.setPrice(payload.getPrice() != null ? payload.getPrice() : 0.0);
					variant.setDiscount(payload.getDiscount() != null ? payload.getDiscount() : 0.0);
					variant.setStock(variantStock);
					variant.setStatus(true); // Default active
					Variant savedVariant = variantRepository.save(variant);

					// 2. Map Attributes & Values
					if (payload.getAttributes() != null) {
						for (java.util.Map.Entry<String, String> entry : payload.getAttributes().entrySet()) {
							String attrName = entry.getKey();
							String attrVal = entry.getValue();

							// Find or create Attribute
							Attribute attribute = attributeRepository.findAll().stream()
									.filter(a -> a.getName().equalsIgnoreCase(attrName)).findFirst().orElse(null);
							if (attribute == null) {
								attribute = new Attribute();
								attribute.setName(attrName);
								attribute = attributeRepository.save(attribute);
							}

							// Find or create AttributeValue
							final Attribute finalAttr = attribute;
							AttributeValue valueObj = attributeValueRepository.findAll().stream()
									.filter(av -> av.getAttribute().getId().equals(finalAttr.getId())
											&& av.getValue().equalsIgnoreCase(attrVal))
									.findFirst().orElse(null);
							if (valueObj == null) {
								valueObj = new AttributeValue();
								valueObj.setValue(attrVal);
								valueObj.setAttribute(attribute);
								valueObj = attributeValueRepository.save(valueObj);
							}

							// Link Variant to AttributeValue
							VariantAttributeValue vav = new VariantAttributeValue();
							vav.setVariant(savedVariant);
							vav.setAttributeValue(valueObj);
							variantAttributeValueRepository.save(vav);
						}
					}

					// 3. Handle Variant Images from dynamic input names
					if (payload.getImageInputName() != null) {
						org.springframework.web.multipart.MultipartFile[] variantImagesFiles = req
								.getFiles(payload.getImageInputName())
								.toArray(new org.springframework.web.multipart.MultipartFile[0]);
						if (variantImagesFiles != null && variantImagesFiles.length > 0) {
							for (org.springframework.web.multipart.MultipartFile vi : variantImagesFiles) {
								if (!vi.isEmpty()) {
									try {
										String vFilename = fileUploadUtils.saveImage(vi, "variants");
										VariantImage variantImage = new VariantImage();
										variantImage.setVariant(savedVariant);
										variantImage.setPathImage(vFilename);
										variantImage.setCreateAt(LocalDateTime.now());
										variantImageRepository.save(variantImage);
									} catch (java.io.IOException e) {
										throw new RuntimeException("Lỗi lưu file hình ảnh biến thể", e);
									}
								}
							}
						}
					}

					// 4. Restore Existing Images if any
					if (payload.getExistingImages() != null && !payload.getExistingImages().isEmpty()) {
						for (String existingPath : payload.getExistingImages()) {
							if (existingPath != null && !existingPath.trim().isEmpty()) {
								VariantImage variantImage = new VariantImage();
								variantImage.setVariant(savedVariant);
								variantImage.setPathImage(existingPath);
								variantImage.setCreateAt(LocalDateTime.now());
								variantImageRepository.save(variantImage);
							}
						}
					}
				}

				// Finalize Product Stock Status
				if (totalStock == 0) {
					savedProduct.setStockStatus(0); // Hết phòng
				} else if (totalStock <= 10) {
					savedProduct.setStockStatus(2); // Sắp hết
				} else {
					savedProduct.setStockStatus(1); // Còn hàng
				}
				productRepository.save(savedProduct);

			} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
				e.printStackTrace();
				model.addAttribute("variantError", "Lỗi xử lý dữ liệu JSON biến thể.");
				throw new RuntimeException("Lỗi JSON biến thể", e);
			}
		}

		// Handle Specifications Saving
		if (specificationsJson != null && !specificationsJson.isEmpty() && !specificationsJson.equals("[]")) {
			try {
				ObjectMapper specMapper = new ObjectMapper();
				java.util.List<com.techone.dto.SpecificationPayloadDto> specs = specMapper.readValue(specificationsJson,
						new TypeReference<java.util.List<com.techone.dto.SpecificationPayloadDto>>() {
						});

				if (!specs.isEmpty()) {
					com.techone.model.Specification specification = new com.techone.model.Specification();
					specification.setProduct(savedProduct);
					specification = specificationRepository.save(specification);

					for (com.techone.dto.SpecificationPayloadDto payload : specs) {
						if (payload.getGroupName() != null && !payload.getGroupName().trim().isEmpty()) {
							com.techone.model.SpecificationTitle title = new com.techone.model.SpecificationTitle();
							title.setSpecification(specification);
							title.setName(payload.getGroupName());
							title = specificationTitleRepository.save(title);

							if (payload.getItems() != null) {
								for (com.techone.dto.SpecificationItemDto item : payload.getItems()) {
									if (item.getName() != null && !item.getName().trim().isEmpty()
											&& item.getValue() != null && !item.getValue().trim().isEmpty()) {
										com.techone.model.SpecificationValue val = new com.techone.model.SpecificationValue();
										val.setSpecificationTitle(title);
										val.setName(item.getName() + ": " + item.getValue()); // Or adjust if the schema
																								// splits Name and
																								// Value. The schema
																								// shows only "name"
																								// field for Value.
										specificationValueRepository.save(val);
									}
								}
							}
						}
					}
				}
			} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
				e.printStackTrace();
				model.addAttribute("specError", "Lỗi xử lý dữ liệu JSON thông số kỹ thuật.");
				throw new RuntimeException("Lỗi JSON thông số", e);
			}
		}

		return "redirect:/admin/product-list?" + (isEdit ? "updated=true" : "success=true");
	}

	private void loadFormAttributes(Model model) {
		model.addAttribute("categories", categoryRepository.findByTypeAndStatus(true, true)); // All Active Product
																								// Categories
		model.addAttribute("parentCategories", categoryRepository.findByTypeAndParentIsNullAndStatus(true, true)); // Active
																													// Parent
																													// Product
																													// Categories
		model.addAttribute("brands", brandRepository.findByStatus(true)); // Active Brands

		// Add default values for Thymeleaf script variables if they are not already
		// present in the model
		if (!model.containsAttribute("variantsJson")) {
			model.addAttribute("variantsJson", "[]");
		}
		if (!model.containsAttribute("specificationsJson")) {
			model.addAttribute("specificationsJson", "[]");
		}
		if (!model.containsAttribute("variantError")) {
			model.addAttribute("variantError", "");
		}
		if (!model.containsAttribute("variantFieldErrors")) {
			model.addAttribute("variantFieldErrors", "{}");
		}
	}

}
