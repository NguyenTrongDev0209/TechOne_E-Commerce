package com.techone.controller.admin;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.techone.model.Brand;
import com.techone.repository.BrandRepository;

import jakarta.validation.Valid;
import com.techone.utils.FileUploadUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BrandManagerController {

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    FileUploadUtils fileUploadUtils;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message",
                "Dung lượng file quá giới hạn cho phép (1MB). Vui lòng chọn ảnh nhỏ hơn.");
        return "redirect:/admin/brand-manager";
    }

    @GetMapping("/admin/brand-manager")
    public String brandManager(Model model,
            @RequestParam("pageNum") Optional<Integer> pageNum,
            @RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("keyword") Optional<String> keyword) {

        int pageNumber = pageNum.orElse(0);
        int pageSizes = pageSize.orElse(5);
        String searchKeyword = keyword.orElse(null);

        Pageable pageable = PageRequest.of(pageNumber, pageSizes, Direction.DESC, "id");
        Page<Brand> page = brandRepository.search(searchKeyword, pageable);

        model.addAttribute("list", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("brand", new Brand());
        model.addAttribute("keyword", searchKeyword);
        model.addAttribute("active", "brands");
        model.addAttribute("editMode", false);

        return "views/admin/manage-brands";
    }

    @PostMapping("/admin/brand/add")
    public String add(Model model,
            @RequestParam(value = "logoFile", required = false) MultipartFile image,
            @ModelAttribute("brand") @Valid Brand brand,
            Errors errors,
            @RequestParam("pageNum") Optional<Integer> pageNum,
            @RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("keyword") Optional<String> keyword) {

        if (image != null && !image.isEmpty()) {
            try {
                brand.setLogo(fileUploadUtils.saveImage(image, "brands"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean hasOnlyLogoError = errors.getErrorCount() == 1 && errors.hasFieldErrors("logo");
        boolean hasActualErrors = errors.hasErrors() && !(hasOnlyLogoError && image != null && !image.isEmpty());

        if (hasActualErrors) {
            Pageable pageable = PageRequest.of(pageNum.orElse(0), pageSize.orElse(5), Direction.DESC, "id");
            Page<Brand> page = brandRepository.search(keyword.orElse(null), pageable);
            model.addAttribute("list", page.getContent());
            model.addAttribute("page", page);
            model.addAttribute("keyword", keyword.orElse(null));
            model.addAttribute("active", "brands");
            model.addAttribute("editMode", false);
            return "views/admin/manage-brands";
        }

        brand.setStatus(true);
        brandRepository.save(brand);

        return "redirect:/admin/brand-manager?success=true";
    }

    @GetMapping("/admin/brand/edit/{id}")
    public String edit(Model model, @PathVariable("id") Integer id,
            @RequestParam("pageNum") Optional<Integer> pageNum,
            @RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("keyword") Optional<String> keyword) {

        Brand brand = brandRepository.findById(id).orElse(null);
        if (brand != null) {
            model.addAttribute("brand", brand);
            model.addAttribute("editMode", true);
        } else {
            model.addAttribute("brand", new Brand());
            model.addAttribute("editMode", false);
        }

        Pageable pageable = PageRequest.of(pageNum.orElse(0), pageSize.orElse(5), Direction.DESC, "id");
        Page<Brand> page = brandRepository.search(keyword.orElse(null), pageable);
        model.addAttribute("list", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword.orElse(null));
        model.addAttribute("active", "brands");

        return "views/admin/manage-brands";
    }

    @PostMapping("/admin/brand/update")
    public String update(Model model,
            @RequestParam(value = "logoFile", required = false) MultipartFile image,
            @ModelAttribute("brand") @Valid Brand brand,
            Errors errors,
            @RequestParam("pageNum") Optional<Integer> pageNum,
            @RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("keyword") Optional<String> keyword) {

        Brand existing = brandRepository.findById(brand.getId()).orElse(null);
        if (existing != null && !existing.getName().equals(brand.getName())) {
            if (brandRepository.existsByName(brand.getName())) {
                errors.rejectValue("name", "error.brand", "Tên thương hiệu đã tồn tại");
            }
        }

        // Handle logo update logic once
        String logoToSave = null;
        if (image != null && !image.isEmpty()) {
            try {
                logoToSave = fileUploadUtils.saveImage(image, "brands");
                brand.setLogo(logoToSave);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (existing != null) {
            brand.setLogo(existing.getLogo());
        }

        boolean hasActualErrors = errors.hasErrors();
        // If edit mode and we have a logo (either new or existing), ignore logo missing
        // error
        if (errors.hasFieldErrors("logo") && brand.getLogo() != null) {
            if (errors.getErrorCount() == 1) {
                hasActualErrors = false;
            }
        }

        if (hasActualErrors) {
            Pageable pageable = PageRequest.of(pageNum.orElse(0), pageSize.orElse(5), Direction.DESC, "id");
            Page<Brand> page = brandRepository.search(keyword.orElse(null), pageable);
            model.addAttribute("list", page.getContent());
            model.addAttribute("page", page);
            model.addAttribute("keyword", keyword.orElse(null));
            model.addAttribute("active", "brands");
            model.addAttribute("editMode", true);
            return "views/admin/manage-brands";
        }

        if (existing != null) {
            boolean oldStatus = existing.getStatus();
            if (logoToSave != null) {
                existing.setLogo(logoToSave);
            }
            existing.setName(brand.getName());
            existing.setStatus(brand.getStatus());
            brandRepository.save(existing);

            // Nếu trạng thái thay đổi, cập nhật luôn các sản phẩm
            if (oldStatus != existing.getStatus()) {
                productRepository.updateStatusByBrandId(existing.getId(), existing.getStatus());
            }
        }

        return "redirect:/admin/brand-manager?updated=true";
    }

    @GetMapping("/admin/brand/delete/{id}")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes ra) {
        int productCount = brandRepository.countProducts(id);
        if (productCount > 0) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa: Thương hiệu này đang có " + productCount
                    + " sản phẩm. Bạn nên chuyển sang trạng thái Ẩn thay vì xóa!");
            return "redirect:/admin/brand-manager";
        }
        brandRepository.deleteById(id);
        return "redirect:/admin/brand-manager?deleted=true";
    }

    @Autowired
    com.techone.repository.ProductRepository productRepository;

    @GetMapping("/admin/brand/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") Integer id) {
        Brand brand = brandRepository.findById(id).orElse(null);
        if (brand != null) {
            boolean newStatus = !brand.getStatus();
            brand.setStatus(newStatus);
            brandRepository.save(brand);

            // Ẩn tất cả sản phẩm thuộc thương hiệu này
            productRepository.updateStatusByBrandId(id, newStatus);
        }
        return "redirect:/admin/brand-manager?updated=true";
    }

}
