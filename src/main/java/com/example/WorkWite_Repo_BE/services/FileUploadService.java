package com.example.WorkWite_Repo_BE.services;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileUploadService {
    
    /**
     * Upload banner image và trả về URL
     */
    public String uploadBannerImage(MultipartFile file, String uploadDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        String fileName = System.currentTimeMillis() + "_" 
            + StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, uploadPath.resolve(fileName), 
                    StandardCopyOption.REPLACE_EXISTING);
            }
            
            String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();
            
            return baseUrl + "/uploads/banners/" + fileName;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Xóa file cũ khi update
     */
    public void deleteFile(String fileUrl, String uploadDir) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            // Extract filename from URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw - file deletion is not critical
            System.err.println("Failed to delete file: " + e.getMessage());
        }
    }
}
