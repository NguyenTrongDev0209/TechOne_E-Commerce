package com.techone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        exposeDirectory("images", registry);
    }

    private void exposeDirectory(String dirName, ResourceHandlerRegistry registry) {
        String projectPath = System.getProperty("user.dir");
        java.nio.file.Path uploadPath = java.nio.file.Paths.get(projectPath, uploadDir, dirName);

        // Robust check for nested project folder
        // (TechOne_E-Commerce/TechOne_E-Commerce/...)
        java.nio.file.Path nestedFolder = java.nio.file.Paths.get(projectPath, "TechOne_E-Commerce");
        if (java.nio.file.Files.exists(nestedFolder) && java.nio.file.Files.isDirectory(nestedFolder)) {
            uploadPath = java.nio.file.Paths.get(projectPath, "TechOne_E-Commerce", uploadDir, dirName);
        }

        String fullPath = uploadPath.toFile().getAbsolutePath().replace("\\", "/");

        if (!fullPath.startsWith("/")) {
            fullPath = "/" + fullPath;
        }

        registry.addResourceHandler("/" + dirName + "/**")
                .addResourceLocations("file:" + fullPath + "/");
    }
}
