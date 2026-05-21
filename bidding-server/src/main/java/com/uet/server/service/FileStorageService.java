package com.uet.server.service;

import com.uet.common.network.FileUploadData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public class FileStorageService {

    private static final Path UPLOAD_ROOT = Path.of("uploads");

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".webp"
    );

    public String save(FileUploadData fileUploadData, String folderName, String filePrefix) {
        if (fileUploadData == null || fileUploadData.isEmpty()) {
            return null;
        }

        try {
            String extension = getSafeExtension(fileUploadData.getOriginalFileName());

            Path uploadDir = UPLOAD_ROOT.resolve(folderName);
            Files.createDirectories(uploadDir);

            String safePrefix = sanitizeFileName(filePrefix);
            String fileName = safePrefix + "_" + UUID.randomUUID() + extension;

            Path filePath = uploadDir.resolve(fileName);
            Files.write(filePath, fileUploadData.getData());

            return filePath.toAbsolutePath().toString();

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