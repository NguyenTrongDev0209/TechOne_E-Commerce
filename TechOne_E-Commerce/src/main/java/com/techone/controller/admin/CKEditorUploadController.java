package com.techone.controller.admin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

@RestController
public class CKEditorUploadController {

    private final String UPLOAD_DIR = "src/main/resources/static/images/posts/";

    @PostMapping("/api/upload/ckeditor")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("upload") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file != null && !file.isEmpty()) {
                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                String projectPath = System.getProperty("user.dir");
                Path uploadPath = Paths.get(projectPath, UPLOAD_DIR);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                File serverFile = uploadPath.resolve(filename).toFile();
                file.transferTo(serverFile);

                String imageUrl = "/images/posts/" + filename;
                
                response.put("uploaded", true);
                response.put("url", imageUrl);
                return ResponseEntity.ok(response);
            }
            
            response.put("uploaded", false);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Thư mục không tồn tại hoặc file lỗi.");
            response.put("error", error);
            
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("uploaded", false);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi ném ra từ Server: " + e.getMessage());
            response.put("error", error);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
