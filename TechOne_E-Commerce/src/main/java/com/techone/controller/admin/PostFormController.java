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
import com.techone.repository.AccountRepository;
import com.techone.repository.CategoryRepository;
import jakarta.validation.Valid;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.BindingResult;

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

    private final String UPLOAD_DIR = "src/main/resources/static/images/posts/";

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
            @Valid @ModelAttribute("post") Post post,
            BindingResult result,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestParam(value = "imageFiles", required = false) MultipartFile[] images) {

        // Manual validation for Thumbnail if it's a new post or if the existing thumbnail is missing
        if ((post.getId() == null || post.getThumbnail() == null || post.getThumbnail().isEmpty()) 
            && (thumbnailFile == null || thumbnailFile.isEmpty())) {
            result.rejectValue("thumbnail", "NotBlank", "Vui lòng tải lên ảnh thumbnail");
        }

        if (result.hasErrors()) {
            loadFormAttributes(model);
            model.addAttribute("editMode", post.getId() != null);
            return "views/admin/post-form";
        }

        // Handle author from session
        jakarta.servlet.http.HttpSession session = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        com.techone.model.Account sessionUser = (com.techone.model.Account) session.getAttribute("user");
        
        if (post.getId() == null && sessionUser != null) {
            post.setAccount(sessionUser);
        } else if (post.getId() == null) {
            // Fallback if no session user (should ideally be protected by security)
            accountRepository.findAll().stream()
                    .filter(a -> a.getRole())
                    .findFirst()
                    .ifPresent(post::setAccount);
        }

        // Set CreateAt and ViewCount if new
        if (post.getId() == null) {
            post.setCreateAt(LocalDate.now());
            post.setViewCount(0);
        }
        
        // Handle Thumbnail
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            try {
                String filename = System.currentTimeMillis() + "_" + thumbnailFile.getOriginalFilename();
                String projectPath = System.getProperty("user.dir");
                Path uploadPath = Paths.get(projectPath, UPLOAD_DIR);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                File file = new File(uploadPath.toFile(), filename);
                thumbnailFile.transferTo(file);
                post.setThumbnail(filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Post savedPost = postRepository.save(post);

        // Handle Editor Images
        if (images != null && images.length > 0) {
            try {
                String projectPath = System.getProperty("user.dir");
                Path uploadPath = Paths.get(projectPath, UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                for (MultipartFile image : images) {
                    if (image != null && !image.isEmpty()) {
                        String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                        File file = new File(uploadPath.toFile(), filename);
                        image.transferTo(file);

                        ImagePost imagePost = new ImagePost();
                        imagePost.setPathImage(filename);
                        imagePost.setPost(savedPost);
                        imagePost.setCreateAt(LocalDate.now());
                        imagePostRepository.save(imagePost);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
