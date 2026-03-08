package com.techone.common.utils;

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

        java.nio.file.Path nestedFolder = java.nio.file.Paths.get(projectPath, "TechOne_E-Commerce");
        if (java.nio.file.Files.exists(nestedFolder) && java.nio.file.Files.isDirectory(nestedFolder)) {
            baseUploadPath = java.nio.file.Paths.get(projectPath, "TechOne_E-Commerce", uploadDir, "images", subDir);
        }

        if (!java.nio.file.Files.exists(baseUploadPath)) {
            java.nio.file.Files.createDirectories(baseUploadPath);
        }

        java.nio.file.Path filePath = baseUploadPath.resolve(fileName);

        try (java.io.InputStream is = file.getInputStream()) {
            java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(is);
            if (originalImage == null) {
                file.transferTo(filePath.toFile());
                return fileName;
            }

            int targetSize = 800;
            java.awt.image.BufferedImage resizedImage = new java.awt.image.BufferedImage(targetSize, targetSize,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = resizedImage.createGraphics();

            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

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
