package com.uet.server.service;

import com.uet.common.network.ImageData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public class FileStorageService {

    private static final Path UPLOAD_ROOT = Path.of(
            System.getProperty(
                    "app.upload.dir",
                    System.getenv().getOrDefault("APP_UPLOAD_DIR", "/root/uploads")  
            )
    ).toAbsolutePath().normalize();

    private static final String PUBLIC_UPLOAD_BASE_URL = System.getProperty(
            "app.upload.base-url",
            System.getenv().getOrDefault(
                    "APP_UPLOAD_BASE_URL",
                    "file://" + UPLOAD_ROOT.toString()  
            )
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".webp"
    );

    public String save(ImageData fileUploadData, String folderName, String filePrefix) {
        if (fileUploadData == null || fileUploadData.isEmpty()) {
            return null;
        }

        try {
            String extension = getSafeExtension(fileUploadData.getOriginalFileName());

            Path uploadDir = UPLOAD_ROOT.resolve(folderName).normalize();
            Files.createDirectories(uploadDir);

            String safePrefix = sanitizeFileName(filePrefix);
            String fileName = safePrefix + "_" + UUID.randomUUID() + extension;

            Path filePath = uploadDir.resolve(fileName).normalize();
            Files.write(filePath, fileUploadData.getData());

            return PUBLIC_UPLOAD_BASE_URL + "/" + folderName + "/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSafeExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return ".png";
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ".png";
        }

        return extension;
    }

    private String sanitizeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "file";
        }

        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}