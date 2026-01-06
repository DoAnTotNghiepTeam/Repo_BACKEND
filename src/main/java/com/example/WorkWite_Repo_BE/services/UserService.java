
package com.example.WorkWite_Repo_BE.services;

import com.example.WorkWite_Repo_BE.dtos.UserDto.*;
import com.example.WorkWite_Repo_BE.entities.Role;
import com.example.WorkWite_Repo_BE.entities.User;
import com.example.WorkWite_Repo_BE.exceptions.HttpException;
import com.example.WorkWite_Repo_BE.repositories.CandidateJpaRepository;
import com.example.WorkWite_Repo_BE.repositories.RoleJpaRepository;
import com.example.WorkWite_Repo_BE.repositories.UserJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserJpaRepository userJpaRepository;


    // Chuẩn hóa hàm convertToDto cho User entity
    private UserResponseDto convertToDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setStatus(user.getStatus());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBalance(String.valueOf(user.getBalance()));
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toList()));
        }
        return dto;
    }

    // public List<UserResponseDto> getAllUsers() {
    //     List<User> users = this.userJpaRepository.findAll();
    //     return users.stream()
    //             .map(this::convertToDto)
    //             .collect(Collectors.toList());
    // }

    // Lấy danh sách user theo phân trang
    public PaginatedUserResponseDto getAllUsersPaginated(int page, int size) {
        // Page số bắt đầu từ 1, chuyển về 0-based cho Pageable
        int pageNumber = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<User> userPage = userJpaRepository.findAll(pageable);

        List<UserResponseDto> userDtos = userPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return PaginatedUserResponseDto.builder()
                .data(userDtos)
                .pageNumber(userPage.getNumber() + 1) // trả về 1-based
                .pageSize(userPage.getSize())
                .totalRecords(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    // Lấy user theo id, trả về DTOre
    public UserResponseDto getUserById(Long id) {
        User user = userJpaRepository.findById(id)
                .orElseThrow(() -> new HttpException("User not found", HttpStatus.NOT_FOUND));
        return convertToDto(user);
    }

    private final SystemLogService systemLogService;


    //  update user
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto request, MultipartFile avatarFile, String username) {
        User user = userJpaRepository.findById(id)
                .orElseThrow(() -> new HttpException("User not found", HttpStatus.NOT_FOUND));
        if (request != null) {
            if (request.getUsername() != null)
                user.setUsername(request.getUsername());
            if (request.getFullName() != null)
                user.setFullName(request.getFullName());
        }

        // Xử lý upload avatar
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // Validate file type
                String contentType = avatarFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new HttpException("File must be an image", HttpStatus.BAD_REQUEST);
                }

                // Tạo tên file unique
                String originalFilename = avatarFile.getOriginalFilename();
                String fileExtension = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg";
                String fileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + fileExtension;

                // Đường dẫn lưu file
                Path uploadPath = Paths.get("uploads/avatars");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Lưu file
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Set đường dẫn vào user
                user.setAvatarUrl("/uploads/avatars/" + fileName);
            } catch (IOException e) {
                throw new HttpException("Failed to upload avatar: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        userJpaRepository.save(user);
        // Log update
        systemLogService.saveLog(
                user.getId(),
                username,
                "UPDATE_USER",
                "User updated",
                "SUCCESS"
        );
        return convertToDto(user);
    }
    public boolean isPending(Long userId) {
        return userJpaRepository.findById(userId)
                .map(user -> "PENDING".equalsIgnoreCase(user.getStatus()))
                .orElse(false);
    }
}
