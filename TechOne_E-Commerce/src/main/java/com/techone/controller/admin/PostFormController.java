package com.techone.controller.admin;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.techone.model.Post;
import com.techone.model.ImagePost;
import com.techone.repository.PostRepository;
import com.techone.repository.ImagePostRepository;
import com.techone.repository.CategoryRepository;
import com.techone.repository.AccountRepository;
import jakarta.servlet.ServletContext;
import jakarta.validation.Valid;

@Controller
public class PostFormController {

    @Autowired
    PostRepository postRepository;

    @Autowired
    ImagePostRepository imagePostRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ServletContext servletContext;

    @GetMapping("/admin/post-list/post-form")
    public String showForm(Model model) {
        Post post = new Post();
        post.setStatus(true);
        post.setCreateAt(LocalDate.now());
        post.setCategory(new com.techone.model.Category());

        model.addAttribute("post", post);
        model.addAttribute("editMode", false);
        loadFormAttributes(model);
        model.addAttribute("menuItem", "posts");
        return "views/admin/post-form";
    }

    @GetMapping("/admin/post-list/post-form/{id}")
    public String editPost(Model model, @PathVariable("id") Integer id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return "redirect:/admin/post-list?error=PostNotFound";
        }
        model.addAttribute("post", post);
        model.addAttribute("editMode", true);
        loadFormAttributes(model);
        model.addAttribute("menuItem", "posts");
        return "views/admin/post-form";
    }

    @PostMapping("/admin/post-list/post-form/save")
    public String savePost(Model model,
            @ModelAttribute("post") @Valid Post post,
            Errors errors,
            @RequestParam(value = "imageFiles", required = false) MultipartFile[] images) {

        if (post.getCategory() == null || post.getCategory().getId() == null) {
            errors.rejectValue("category", "error.post", "Chưa chọn danh mục");
        }

        if (errors.hasErrors()) {
            loadFormAttributes(model);
            model.addAttribute("editMode", post.getId() != null);
            model.addAttribute("menuItem", "posts");
            return "views/admin/post-form";
        }

        // Handle author (Optional: pick first admin if exists for now, or null)
        if (post.getId() == null && post.getAccount() == null) {
            accountRepository.findAll().stream()
                    .filter(a -> a.getRole())
                    .findFirst()
                    .ifPresent(post::setAccount);
        }

        // Set CreateAt if new
        if (post.getId() == null) {
            post.setCreateAt(LocalDate.now());
        }

        Post savedPost = postRepository.save(post);

        // Handle Images
        if (images != null && images.length > 0) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    try {
                        String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                        String path = servletContext.getRealPath("/images/posts/");

                        if (path != null) {
                            java.io.File dir = new java.io.File(path);
                            if (!dir.exists())
                                dir.mkdirs();

                            java.io.File file = new java.io.File(path + java.io.File.separator + filename);
                            image.transferTo(file);

                            ImagePost imagePost = new ImagePost();
                            imagePost.setPathImage(filename);
                            imagePost.setPost(savedPost);
                            imagePost.setCreateAt(LocalDate.now());
                            imagePostRepository.save(imagePost);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        boolean isEdit = post.getId() != null;
        return "redirect:/admin/post-list?" + (isEdit ? "updated=true" : "success=true");
    }

    private void loadFormAttributes(Model model) {
        // type = false means Post Category
        model.addAttribute("postCategories", categoryRepository.findByTypeAndStatus(false, true));
    }
}
