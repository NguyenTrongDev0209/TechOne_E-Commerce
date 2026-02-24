package com.techone.controller.admin;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.techone.model.Category;
import com.techone.repository.CategoryRepository;

import jakarta.servlet.ServletContext;
import jakarta.validation.Valid;

@Controller
public class CategoryManagerController {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ServletContext servletContext;

    @GetMapping("/admin/category-manager")
    public String ListCategory(Model model,
            @RequestParam("pageNum") Optional<Integer> pageNum,
            @RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("type") Optional<Boolean> type,
            @RequestParam("keyword") Optional<String> keyword,
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> toDate) {

        int pageNumber = pageNum.orElse(0);
        int pageSizes = pageSize.orElse(5);
        Boolean categoryType = type.orElse(true); // Default to Product Category (true)
        String searchKeyword = keyword.orElse(null);
        LocalDate searchFromDate = fromDate.orElse(null);
        LocalDate searchToDate = toDate.orElse(null);

        Pageable pageable = PageRequest.of(pageNumber, pageSizes, Direction.DESC, "id");

        // Filter by Type (Only Root Categories) + Search
        Page<Category> page = categoryRepository.search(searchKeyword, searchFromDate, searchToDate, categoryType,
                pageable);

        // Get parent list (only same type AND root categories) - Keep this for Add Form
        // dropdown
        List<Category> parentList = categoryRepository.findByTypeAndParentIsNull(categoryType);

        List<Category> categories = page.getContent();

        Category category = new Category();
        category.setType(categoryType);

        model.addAttribute("list", categories);
        model.addAttribute("page", page);
        model.addAttribute("parentList", parentList);
        model.addAttribute("type", categoryType);
        model.addAttribute("category", category); // For Add Form
        model.addAttribute("active", "categories");
        model.addAttribute("keyword", searchKeyword);
        model.addAttribute("fromDate", searchFromDate);
        model.addAttribute("toDate", searchToDate);

        return "views/admin/manage-categories";
    }

    @GetMapping("/admin/category/add")
    public String add() {
        return "redirect:/admin/category-manager";
    }

    @PostMapping("/admin/category/add")
    public String add(Model model,
            @RequestParam(value = "iconFile", required = false) MultipartFile image,
            @ModelAttribute("category") @Valid Category category,
            Errors errors,
            @RequestParam("pageNum") Optional<Integer> pageNum,
            @RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("keyword") Optional<String> keyword,
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> toDate) {

        if (image == null || image.isEmpty()) {
            errors.rejectValue("image", "error.category", "Hình ảnh không được để trống");
        } else if (image.getSize() > 2 * 1024 * 1024) {
            errors.rejectValue("image", "error.category", "Dung lượng ảnh không được vượt quá 2MB");
        }

        // Duplicate Check
        if (categoryRepository.existsByNameAndType(category.getName(), category.getType())) {
            errors.rejectValue("name", "error.category", "Tên danh mục đã tồn tại trong loại này");
        }
        if (category.getSlug() != null && !category.getSlug().isEmpty()) {
            if (categoryRepository.existsBySlugAndType(category.getSlug(), category.getType())) {
                errors.rejectValue("slug", "error.category", "Đường dẫn (Slug) đã tồn tại trong loại này");
            }
        }

        if (errors.hasErrors()) {
            int pageNumber = pageNum.orElse(0);
            int pageSizes = pageSize.orElse(5);
            Boolean categoryType = category.getType() != null ? category.getType() : true;
            String searchKeyword = keyword.orElse(null);
            LocalDate searchFromDate = fromDate.orElse(null);
            LocalDate searchToDate = toDate.orElse(null);

            Pageable pageable = PageRequest.of(pageNumber, pageSizes, Direction.DESC, "id");
            Page<Category> page = categoryRepository.search(searchKeyword, searchFromDate, searchToDate, categoryType,
                    pageable);
            List<Category> parentList = categoryRepository.findByTypeAndParentIsNull(categoryType);
            List<Category> categories = page.getContent();

            model.addAttribute("list", categories);
            model.addAttribute("page", page);
            model.addAttribute("parentList", parentList);
            model.addAttribute("type", categoryType);
            model.addAttribute("keyword", searchKeyword);
            model.addAttribute("fromDate", searchFromDate);
            model.addAttribute("toDate", searchToDate);
            model.addAttribute("active", "categories");

            return "views/admin/manage-categories";
        } else {
            if (image != null && !image.isEmpty()) {
                try {
                    String filename = image.getOriginalFilename();
                    String path = servletContext.getRealPath("/images/categories/");

                    if (path != null) {
                        File dir = new File(path);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File file = new File(path + File.separator + filename);
                        image.transferTo(file);
                        category.setImage(filename);
                    } else {
                        // Fallback or log error: cannot upload to null path
                        System.err.println("Error: servletContext.getRealPath returned null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Optional: return error to user if image upload is critical
                }
            }

            // Fix TransientPropertyValueException
            if (category.getParent() != null) {
                if (category.getParent().getId() == null) {
                    category.setParent(null);
                } else {
                    // Reload parent to ensure it is managed
                    Category parent = categoryRepository.findById(category.getParent().getId()).orElse(null);
                    category.setParent(parent);
                }
            }

            // Generate Slug if empty
            if (category.getSlug() == null || category.getSlug().isEmpty()) {
                category.setSlug(com.techone.utils.SlugUtils.toSlug(category.getName()));
            }

            category.setStatus(true);
            // Type is bound from form
            try {
                categoryRepository.save(category);
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/admin/category-manager?message=Error: Duplicate Name or Slug&type="
                        + category.getType();
            }

            return "redirect:/admin/category-manager?success=true&type=" + category.getType();
        }
    }

    @GetMapping("/admin/category/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") Integer id,
            @RequestParam(name = "type", required = false, defaultValue = "true") Boolean type) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            boolean newStatus = !category.getStatus();
            updateCategoryStatusRecursive(category, newStatus);
        }
        return "redirect:/admin/category-manager?updated=true&type=" + type;
    }

    @GetMapping("/admin/category/hide/{id}")
    public String hideCategory(@PathVariable("id") Integer id,
            @RequestParam(name = "type", required = false, defaultValue = "true") Boolean type) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            updateCategoryStatusRecursive(category, false);
        }
        return "redirect:/admin/category-manager?updated=true&type=" + type;
    }

    private void updateCategoryStatusRecursive(Category category, boolean status) {
        category.setStatus(status);
        if (category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                updateCategoryStatusRecursive(child, status);
            }
        }
        categoryRepository.save(category);
    }

    @GetMapping("/admin/category/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
            @RequestParam(name = "type", required = false, defaultValue = "true") Boolean type) {

        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return "redirect:/admin/category-manager?message=Danh mục không tồn tại&type=" + type;
        }

        // Check for children
        int childCount = categoryRepository.countByParentId(id);
        if (childCount > 0) {
            return "redirect:/admin/category-manager?message=Không thể xóa: Danh mục còn " + childCount
                    + " danh mục con&type=" + type;
        }

        // Check for products
        int productCount = categoryRepository.countProducts(id);
        if (productCount > 0) {
            return "redirect:/admin/category-manager?message=Không thể xóa: Danh mục còn " + productCount
                    + " sản phẩm liên quan&type=" + type;
        }

        // Check for posts
        int postCount = categoryRepository.countPosts(id);
        if (postCount > 0) {
            return "redirect:/admin/category-manager?message=Không thể xóa: Danh mục còn " + postCount
                    + " bài viết liên quan&type=" + type;
        }

        try {
            categoryRepository.deleteById(id);
            return "redirect:/admin/category-manager?deleted=true&type=" + type;
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/category-manager?message=Lỗi hệ thống: Không thể xóa do ràng buộc dữ liệu ngầm (FK Constraint)&type="
                    + type;
        }
    }

    @GetMapping("/admin/category/edit/{id}")
    public String edit(Model model, @PathVariable("id") Integer id, @RequestParam("type") Optional<Boolean> type,
            @RequestParam("keyword") Optional<String> keyword,
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> toDate) {
        Category category = categoryRepository.findById(id).orElse(null);

        if (category != null) {
            model.addAttribute("category", category);
            model.addAttribute("editMode", true);
        }

        // Reuse List logic to show the table below
        int pageNumber = 0;
        int pageSizes = 5;
        Boolean categoryType = type.orElse(true);
        String searchKeyword = keyword.orElse(null);
        LocalDate searchFromDate = fromDate.orElse(null);
        LocalDate searchToDate = toDate.orElse(null);

        Pageable pageable = PageRequest.of(pageNumber, pageSizes, Direction.DESC, "id");
        Page<Category> page = categoryRepository.search(searchKeyword, searchFromDate, searchToDate, categoryType,
                pageable);
        List<Category> parentList = categoryRepository.findByTypeAndParentIsNull(categoryType);
        List<Category> categories = page.getContent();

        model.addAttribute("list", categories);
        model.addAttribute("page", page);
        model.addAttribute("parentList", parentList);
        model.addAttribute("type", categoryType);
        model.addAttribute("active", "categories");
        model.addAttribute("keyword", searchKeyword);
        model.addAttribute("fromDate", searchFromDate);
        model.addAttribute("toDate", searchToDate);

        return "views/admin/manage-categories";
    }

    @PostMapping("/admin/category/update")
    public String update(Model model,
            @RequestParam(value = "iconFile", required = false) MultipartFile image,
            @ModelAttribute("category") @Valid Category category,
            Errors errors,
            @RequestParam("pageNum") Optional<Integer> pageNum,
            @RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("keyword") Optional<String> keyword,
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> toDate) {

        // Duplicate Check
        Category existingByName = categoryRepository.findByNameAndType(category.getName(), category.getType());
        if (existingByName != null && !existingByName.getId().equals(category.getId())) {
            errors.rejectValue("name", "error.category", "Tên danh mục đã tồn tại trong loại này");
        }
        if (category.getSlug() != null && !category.getSlug().isEmpty()) {
            Category existingBySlug = categoryRepository.findBySlugAndType(category.getSlug(), category.getType());
            if (existingBySlug != null && !existingBySlug.getId().equals(category.getId())) {
                errors.rejectValue("slug", "error.category", "Đường dẫn (Slug) đã tồn tại trong loại này");
            }
        }

        if (errors.hasErrors()) {
            int pageNumber = pageNum.orElse(0);
            int pageSizes = pageSize.orElse(5);
            Boolean categoryType = category.getType() != null ? category.getType() : true;
            String searchKeyword = keyword.orElse(null);
            LocalDate searchFromDate = fromDate.orElse(null);
            LocalDate searchToDate = toDate.orElse(null);

            // Restore existing data (image)
            if (category.getId() != null) {
                Category existing = categoryRepository.findById(category.getId()).orElse(null);
                if (existing != null && category.getImage() == null) {
                    category.setImage(existing.getImage());
                }
            }

            Pageable pageable = PageRequest.of(pageNumber, pageSizes, Direction.DESC, "id");
            Page<Category> page = categoryRepository.search(searchKeyword, searchFromDate, searchToDate, categoryType,
                    pageable);
            List<Category> parentList = categoryRepository.findByTypeAndParentIsNull(categoryType);
            List<Category> categories = page.getContent();

            model.addAttribute("list", categories);
            model.addAttribute("page", page);
            model.addAttribute("parentList", parentList);
            model.addAttribute("type", categoryType);
            model.addAttribute("keyword", searchKeyword);
            model.addAttribute("fromDate", searchFromDate);
            model.addAttribute("toDate", searchToDate);
            model.addAttribute("active", "categories");
            model.addAttribute("editMode", true);

            return "views/admin/manage-categories";
        }

        Category existing = categoryRepository.findById(category.getId()).orElse(null);
        if (existing != null) {
            if (image != null && !image.isEmpty()) {
                try {
                    String filename = image.getOriginalFilename();
                    String path = servletContext.getRealPath("/images/categories/");

                    if (path != null) {
                        File dir = new File(path);
                        if (!dir.exists())
                            dir.mkdirs();

                        File file = new File(path + File.separator + filename);
                        image.transferTo(file);
                        existing.setImage(filename);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            existing.setName(category.getName());

            // Fix TransientPropertyValueException
            if (category.getParent() != null) {
                if (category.getParent().getId() == null) {
                    category.setParent(null);
                } else {
                    Category parent = categoryRepository.findById(category.getParent().getId()).orElse(null);
                    category.setParent(parent);
                }
            }
            existing.setParent(category.getParent());
            // Existing status and type should ideally be preserved or updated if form has
            // them
            // category object from form might not have all fields if they are not in form

            if (category.getSlug() == null || category.getSlug().isEmpty()) {
                existing.setSlug(com.techone.utils.SlugUtils.toSlug(category.getName()));
            } else {
                existing.setSlug(category.getSlug());
            }

            try {
                categoryRepository.save(existing);
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/admin/category-manager?message=Error: Duplicate Name or Slug&type="
                        + category.getType();
            }
        }
        return "redirect:/admin/category-manager?updated=true&type=" + category.getType();
    }
}
