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
	com.techone.repository.OrderDetailRepository orderDetailRepository;

	@Autowired
	FileUploadUtils fileUploadUtils;

	@Autowired
	com.techone.repository.CartItemRepository cartItemRepository;

	@Autowired
	com.techone.repository.FavouriteRepository favouriteRepository;

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
					// IMPORTANT: Only load active variants for the form
					if (v.getStatus() != null && !v.getStatus()) {
						continue;
					}

					VariantPayloadDto dto = new VariantPayloadDto();
					dto.setId(v.getId() != null ? v.getId().longValue() : null);
					dto.setSku(v.getSku());
					dto.setPrice(v.getPrice());
					dto.setDiscount(v.getDiscount());
					dto.setStock(v.getStock());

					java.util.Map<String, String> attrs = new java.util.HashMap<>();
					if (v.getVariantAttributeValues() != null) {
						for (VariantAttributeValue vav : v.getVariantAttributeValues()) {
							if (vav.getAttributeValue() != null && vav.getAttributeValue().getAttribute() != null) {
								attrs.put(vav.getAttributeValue().getAttribute().getName(),
										vav.getAttributeValue().getValue());
							}
						}
					}
					// If no attributes, it's a default/standard variant
					if (attrs.isEmpty()) {
						attrs.put("Mặc định", "Mặc định");
					}

					if (v.getVariantImages() != null && !v.getVariantImages().isEmpty()) {
						dto.setExistingImages(v.getVariantImages().stream().map(VariantImage::getPathImage)
								.collect(java.util.stream.Collectors.toList()));
					}

					dto.setAttributes(attrs);
					dto.setImageInputName("variantImages_" + v.getId());
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

	@org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
	@PostMapping("/admin/product-list/product-form/save")
	public String saveProduct(@ModelAttribute("product") @Valid Product product, Errors errors, Model model,
			@jakarta.validation.constraints.NotNull @org.springframework.web.bind.annotation.RequestParam("variantsJson") String variantsJson,
			@jakarta.validation.constraints.NotNull @org.springframework.web.bind.annotation.RequestParam("specificationsJson") String specificationsJson,
			@org.springframework.web.bind.annotation.RequestParam(value = "imageFiles", required = false) org.springframework.web.multipart.MultipartFile[] images,
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
				boolean isCategoryHidden = cat != null && (Boolean.FALSE.equals(cat.getStatus())
						|| (cat.getParent() != null && Boolean.FALSE.equals(cat.getParent().getStatus())));
				if (isCategoryHidden) {
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

					// Validate Variant Images (existing or new)
					boolean hasImage = false;
					if (payload.getExistingImages() != null && !payload.getExistingImages().isEmpty()) {
						hasImage = true;
					}

					if (!hasImage && payload.getImageInputName() != null) {
						java.util.List<org.springframework.web.multipart.MultipartFile> variantImagesFiles = req
								.getFiles(payload.getImageInputName());
						if (variantImagesFiles != null) {
							for (org.springframework.web.multipart.MultipartFile f : variantImagesFiles) {
								if (!f.isEmpty()) {
									hasImage = true;
									break;
								}
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

		// Determine if this is an edit or new creation
		boolean isEdit = product.getId() != null;
		if (isEdit) {
			Product existing = productRepository.findById(product.getId()).orElse(null);
			if (existing != null) {
				product.setCreateAt(existing.getCreateAt());
				product.setAccount(existing.getAccount());
			}
		} else {
			product.setCreateAt(LocalDateTime.now());
			Account currentUser = SessionUtils.get("user");
			if (currentUser != null) {
				product.setAccount(currentUser);
			}
		}

		Product savedProduct = productRepository.save(product);

		// 1. Handle Variants Updating Logic
		if (variantsJson != null && !variantsJson.isEmpty() && !variantsJson.equals("[]")) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				java.util.List<VariantPayloadDto> variantPayloads = mapper.readValue(variantsJson,
						new TypeReference<java.util.List<VariantPayloadDto>>() {
						});

				java.util.List<Variant> currentVariants = variantRepository.findByProduct(savedProduct);
				java.util.Map<Integer, Variant> existingVariantMap = new java.util.HashMap<>();
				if (currentVariants != null) {
					for (Variant cv : currentVariants) {
						existingVariantMap.put(cv.getId(), cv);
					}
				}

				java.util.Set<Integer> incomingIds = new java.util.HashSet<>();
				int totalStock = 0;

				for (VariantPayloadDto payload : variantPayloads) {
					Variant variant;
					Long pId = payload.getId();
					if (pId != null && pId <= Integer.MAX_VALUE && existingVariantMap.containsKey(pId.intValue())) {
						variant = existingVariantMap.get(pId.intValue());
						incomingIds.add(pId.intValue());
					} else {
						variant = new Variant();
						variant.setProduct(savedProduct);
					}

					variant.setSku(payload.getSku());
					variant.setPrice(payload.getPrice() != null ? payload.getPrice() : 0.0);
					variant.setDiscount(payload.getDiscount() != null ? payload.getDiscount() : 0.0);
					variant.setStock(payload.getStock() != null ? payload.getStock() : 0);
					variant.setStatus(true);
					totalStock += variant.getStock();

					// If it's a new variant, ensure it's in the product's collection for proper
					// cascading/management
					if (payload.getId() == null || payload.getId() > Integer.MAX_VALUE) {
						if (savedProduct.getVariant() == null) {
							savedProduct.setVariant(new java.util.ArrayList<>());
						}
						// Avoid duplicates if this loop is somehow re-entered or if it's already there
						if (!savedProduct.getVariant().contains(variant)) {
							savedProduct.getVariant().add(variant);
						}
					}

					// Handle Attributes for this variant
					if (variant.getVariantAttributeValues() != null) {
						variant.getVariantAttributeValues().clear();
					} else {
						variant.setVariantAttributeValues(new java.util.ArrayList<>());
					}

					if (payload.getAttributes() != null) {
						for (java.util.Map.Entry<String, String> entry : payload.getAttributes().entrySet()) {
							String attrName = entry.getKey();
							String attrVal = entry.getValue();

							Attribute attribute = attributeRepository.findAll().stream()
									.filter(a -> a.getName().equalsIgnoreCase(attrName)).findFirst().orElse(null);
							if (attribute == null) {
								attribute = new Attribute();
								attribute.setName(attrName);
								attribute = attributeRepository.save(attribute);
							}

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

							VariantAttributeValue vav = new VariantAttributeValue();
							vav.setVariant(variant);
							vav.setAttributeValue(valueObj);
							variant.getVariantAttributeValues().add(vav);
						}
					}

					// 1. Remove existing images that are no longer in payload.existingImages
					if (variant.getVariantImages() != null) {
						java.util.List<String> payloadExisting = payload.getExistingImages() != null
								? payload.getExistingImages()
								: new java.util.ArrayList<>();

						java.util.List<VariantImage> imagesToRemove = new java.util.ArrayList<>();
						for (VariantImage vi : variant.getVariantImages()) {
							if (!payloadExisting.contains(vi.getPathImage())) {
								fileUploadUtils.deleteFile(vi.getPathImage(), "variants");
								imagesToRemove.add(vi);
							}
						}
						variant.getVariantImages().removeAll(imagesToRemove);
					}

					// 2. Handle New Images for this variant
					if (payload.getImageInputName() != null) {
						java.util.List<org.springframework.web.multipart.MultipartFile> variantImagesFiles = req
								.getFiles(payload.getImageInputName());
						if (variantImagesFiles != null) {
							for (org.springframework.web.multipart.MultipartFile vi : variantImagesFiles) {
								if (!vi.isEmpty()) {
									try {
										String vFilename = fileUploadUtils.saveImage(vi, "variants");
										VariantImage variantImage = new VariantImage();
										variantImage.setVariant(variant);
										variantImage.setPathImage(vFilename);
										variantImage.setCreateAt(LocalDateTime.now());
										if (variant.getVariantImages() == null) {
											variant.setVariantImages(new java.util.ArrayList<>());
										}
										variant.getVariantImages().add(variantImage);
									} catch (java.io.IOException e) {
										throw new RuntimeException("Lỗi lưu file hình ảnh biến thể", e);
									}
								}
							}
						}
					}

					// NOW save the fully populated variant
					variant = variantRepository.save(variant);
					if (variant.getId() != null) {
						incomingIds.add(variant.getId());
					}
				}

				// Deleting old variants that are not in the payload
				if (savedProduct.getVariant() != null) {
					java.util.Iterator<Variant> it = savedProduct.getVariant().iterator();
					while (it.hasNext()) {
						Variant cv = it.next();
						if (!incomingIds.contains(cv.getId())) {
							try {
								// Physically delete variant images before deleting the variant
								if (cv.getVariantImages() != null) {
									for (VariantImage vImg : cv.getVariantImages()) {
										fileUploadUtils.deleteFile(vImg.getPathImage(), "variants");
									}
								}
								// Remove from parent collection first to avoid re-insertion on flush
								it.remove();
								// Then perform hard delete
								variantRepository.delete(cv);
								variantRepository.flush();
							} catch (org.springframework.dao.DataIntegrityViolationException e) {
								// If hard delete fails, re-add to collection (since it's not orphanRemoval
								// anymore)
								// but it's still in the DB, so we just Soft Delete it.
								cv.setStatus(false);
								variantRepository.save(cv);
							}
						}
					}
				}

				// 5. Cleanup removed Variants
				for (Variant oldV : existingVariants) {
					if (!keptIds.contains(oldV.getId())) {
						boolean hasOrders = !orderDetailRepository.findByVariant(oldV).isEmpty();
						boolean hasCart = cartItemRepository.existsByVariant(oldV);
						boolean hasFavourite = favouriteRepository.existsByVariant(oldV);

						if (hasOrders || hasCart || hasFavourite) {
							oldV.setStatus(false);
							variantRepository.save(oldV);
						} else {
							java.util.List<VariantAttributeValue> vavs = variantAttributeValueRepository
									.findByVariant(oldV);
							if (vavs != null)
								variantAttributeValueRepository.deleteAll(vavs);

							java.util.List<VariantImage> vImages = variantImageRepository.findByVariant(oldV);
							if (vImages != null)
								variantImageRepository.deleteAll(vImages);

							variantRepository.delete(oldV);
						}
					}
				}

				if (totalStock == 0) {
					savedProduct.setStockStatus(0);
				} else if (totalStock <= 10) {
					savedProduct.setStockStatus(2);
				} else {
					savedProduct.setStockStatus(1);
				}
				productRepository.save(savedProduct);

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Lỗi xử lý biến thể sản phẩm", e);
			}
		}

		// 2. Handle Specifications Updating Logic
		if (specificationsJson != null && !specificationsJson.isEmpty() && !specificationsJson.equals("[]")) {
			try {
				ObjectMapper specMapper = new ObjectMapper();
				java.util.List<com.techone.dto.SpecificationPayloadDto> specPayloads = specMapper.readValue(
						specificationsJson,
						new TypeReference<java.util.List<com.techone.dto.SpecificationPayloadDto>>() {
						});

				// Clean up old ones for the product (using orphanRemoval)
				if (savedProduct.getSpecificationList() != null) {
					savedProduct.getSpecificationList().clear();
				}

				if (!specPayloads.isEmpty()) {
					com.techone.model.Specification specification = new com.techone.model.Specification();
					specification.setProduct(savedProduct);
					specification = specificationRepository.save(specification);

					for (com.techone.dto.SpecificationPayloadDto payload : specPayloads) {
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
										val.setName(item.getName() + ": " + item.getValue());
										specificationValueRepository.save(val);
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Lỗi xử lý thông số kỹ thuật", e);
			}
		}

		// 3. Handle General Product Images (if any)
		if (hasNewImages) {
			// Delete existing product images physically and from DB
			if (savedProduct.getImageProduct() != null) {
				for (com.techone.model.ImageProduct ei : savedProduct.getImageProduct()) {
					fileUploadUtils.deleteFile(ei.getUrl(), "products");
				}
				savedProduct.getImageProduct().clear();
			}

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

		return "redirect:/admin/product-list?" + (isEdit ? "updated=true" : "success=true");
	}

	private void loadFormAttributes(Model model) {
		model.addAttribute("categories", categoryRepository.findByTypeAndStatusAndParentActive(true, true)); // All
																												// Active
																												// Product
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
