package com.techone.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileUploadUtils {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String saveImage(MultipartFile file, String subDir) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String projectPath = System.getProperty("user.dir");
        java.nio.file.Path baseUploadPath = java.nio.file.Paths.get(projectPath, uploadDir, "images", subDir);

        // Robust check for nested project folder
        // (TechOne_E-Commerce/TechOne_E-Commerce/...)
        java.nio.file.Path nestedFolder = java.nio.file.Paths.get(projectPath, "TechOne_E-Commerce");
        if (java.nio.file.Files.exists(nestedFolder) && java.nio.file.Files.isDirectory(nestedFolder)) {
            java.nio.file.Path alternativePath = java.nio.file.Paths.get(projectPath, "TechOne_E-Commerce", uploadDir,
                    "images", subDir);
            baseUploadPath = alternativePath;
        }

        if (!java.nio.file.Files.exists(baseUploadPath)) {
            java.nio.file.Files.createDirectories(baseUploadPath);
        }

        java.nio.file.Path filePath = baseUploadPath.resolve(fileName);

        // Resize image to fixed size (e.g., 800x800) to prevent distortion and ensure
        // consistency
        try (java.io.InputStream is = file.getInputStream()) {
            java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(is);
            if (originalImage == null) {
                // Not an image file, fall back to simple transfer if it's some other file type
                // or just throw error
                file.transferTo(filePath.toFile());
                return fileName;
            }

            int targetSize = 800; // Fixed size
            java.awt.image.BufferedImage resizedImage = new java.awt.image.BufferedImage(targetSize, targetSize,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = resizedImage.createGraphics();

            // Set rendering hints for better quality
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            // Calculate scaling to cover the 800x800 area (object-fit: cover style)
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            double scale = Math.max((double) targetSize / originalWidth, (double) targetSize / originalHeight);
            int scaledWidth = (int) (originalWidth * scale);
            int scaledHeight = (int) (originalHeight * scale);
            int x = (targetSize - scaledWidth) / 2;
            int y = (targetSize - scaledHeight) / 2;

            g2d.drawImage(originalImage, x, y, scaledWidth, scaledHeight, null);
            g2d.dispose();

            String formatName = "jpg";
            if (file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".png")) {
                formatName = "png";
            }
            javax.imageio.ImageIO.write(resizedImage, formatName, filePath.toFile());
        }

        return fileName;
    }

    public void deleteFile(String fileName, String subDir) {
        if (fileName == null || fileName.isEmpty())
            return;
        try {
            String projectPath = System.getProperty("user.dir");
            java.nio.file.Path filePath = java.nio.file.Paths.get(projectPath, uploadDir, "images", subDir, fileName);

            java.nio.file.Path nestedFolder = java.nio.file.Paths.get(projectPath, "TechOne_E-Commerce");
            if (java.nio.file.Files.exists(nestedFolder) && java.nio.file.Files.isDirectory(nestedFolder)) {
                filePath = java.nio.file.Paths.get(projectPath, "TechOne_E-Commerce", uploadDir, "images", subDir,
                        fileName);
            }

            java.nio.file.Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Could not delete file: " + fileName + ". " + e.getMessage());
        }
    }
}
