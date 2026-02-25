package com.techone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        exposeDirectory("images", registry);
    }

    private void exposeDirectory(String dirName, ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir, dirName);
        String fullPath = uploadPath.toFile().getAbsolutePath();

        if (fullPath.startsWith("/")) {
            fullPath = "file:" + fullPath;
        } else {
            fullPath = "file:/" + fullPath;
        }

        registry.addResourceHandler("/" + dirName + "/**")
                .addResourceLocations(fullPath + "/");
    }
}
