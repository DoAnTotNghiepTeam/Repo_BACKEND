
package com.example.WorkWite_Repo_BE.controlers;

import com.example.WorkWite_Repo_BE.dtos.BannerDto.BannerRequestDTO;
import com.example.WorkWite_Repo_BE.dtos.BannerDto.BannerUpdateRequestDTO;
import com.example.WorkWite_Repo_BE.dtos.BannerDto.BannerResponseDTO;
import com.example.WorkWite_Repo_BE.services.BannerService;
import com.example.WorkWite_Repo_BE.services.FileUploadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {
    
    // Fields ở đầu class
    private final BannerService bannerService;
    private final FileUploadService fileUploadService;
    private final Validator validator;  // ← Inject Validator
    
    @Value("${banner.upload.dir}")
    private String bannerUploadDir;
    
    // ==========================
    // API Endpoints
    // ==========================
    
    // Lấy danh sách tất cả banner theo phân trang
    @GetMapping("/paginated")
    public ResponseEntity<com.example.WorkWite_Repo_BE.dtos.BannerDto.PaginatedBannerResponseDto> getAllBannersPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bannerService.getAllBannersPaginated(page, size));
    }
    
    // Lấy tất cả banner theo userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BannerResponseDTO>> getBannersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bannerService.getBannersByUserId(userId));
    }
    
    // Lấy banner đang ACTIVE - có thể lọc theo bannerType (Vip/Featured/Standard)
    @GetMapping("/active")
    public ResponseEntity<List<BannerResponseDTO>> getActiveBannerList(
            @RequestParam(required = false) String bannerType) {
        return ResponseEntity.ok(bannerService.getActiveBannerList(bannerType));
    }
    
    // Lấy banner đã được admin DUYỆT (APPROVED) - chờ đến ngày hiển thị
    @GetMapping("/approved")
    public ResponseEntity<List<BannerResponseDTO>> getApprovedBanners() {
        return ResponseEntity.ok(bannerService.getApprovedBanners());
    }
    
    // Lấy banner ACTIVE theo type cụ thể (Vip/Featured/Standard)
    @GetMapping("/type/{bannerType}")
    public ResponseEntity<List<BannerResponseDTO>> getActiveBannersByType(@PathVariable String bannerType) {
        return ResponseEntity.ok(bannerService.getActiveBannersByType(bannerType));
    }
    
    // Lấy danh sách tất cả banner (tất cả status)
    @GetMapping
    public ResponseEntity<List<BannerResponseDTO>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    // Tạo banner (công ty gửi yêu cầu thuê)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<BannerResponseDTO> createBanner(
            @RequestParam String companyName,
            @RequestParam String companyEmail,
            @RequestParam String companyPhone,
            @RequestParam String bannerType,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(value = "bannerImage", required = false) MultipartFile bannerImage
    ) {
        // Upload file ở Controller
        String imageUrl = fileUploadService.uploadBannerImage(bannerImage, bannerUploadDir);
        
        BannerRequestDTO dto = new BannerRequestDTO();
        dto.setCompanyName(companyName);
        dto.setCompanyEmail(companyEmail);
        dto.setCompanyPhone(companyPhone);
        dto.setBannerType(bannerType);
        dto.setStartDate(LocalDate.parse(startDate));
        dto.setEndDate(LocalDate.parse(endDate));
        dto.setBannerImage(imageUrl);
        
        // Trigger validation annotations
        Set<ConstraintViolation<BannerRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException(errors);
        }
        
        BannerResponseDTO response = bannerService.createBanner(dto);
        return ResponseEntity.ok(response);
    }

    // Cập nhật banner (Phone KHÔNG BẮT BUỘC)
    @PatchMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<BannerResponseDTO> updateBanner(
            @PathVariable Long id,
            @RequestParam String companyName,
            @RequestParam String companyEmail,
            @RequestParam(required = false) String companyPhone,  // ← OPTIONAL
            @RequestParam String bannerType,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String bannerImageOld,
            @RequestParam(value = "bannerImage", required = false) MultipartFile bannerImage
    ) {
        String imageUrl = bannerImageOld;
        
        if (bannerImage != null && !bannerImage.isEmpty()) {
            // Xóa file cũ
            fileUploadService.deleteFile(bannerImageOld, bannerUploadDir);
            // Upload file mới
            imageUrl = fileUploadService.uploadBannerImage(bannerImage, bannerUploadDir);
        }
        
        BannerUpdateRequestDTO dto = new BannerUpdateRequestDTO();
        dto.setCompanyName(companyName);
        dto.setCompanyEmail(companyEmail);
        dto.setCompanyPhone(companyPhone);  // có thể null
        dto.setBannerType(bannerType);
        dto.setStartDate(LocalDate.parse(startDate));
        dto.setEndDate(LocalDate.parse(endDate));
        dto.setBannerImage(imageUrl);
        
        // Trigger validation annotations
        Set<ConstraintViolation<BannerUpdateRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException(errors);
        }
        
        BannerResponseDTO response = bannerService.updateBanner(id, dto);
        return ResponseEntity.ok(response);
    }

    // Xóa banner
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }

    // Admin duyệt banner
    @PatchMapping("/{id}/approve")
    public ResponseEntity<BannerResponseDTO> approveBanner(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.approveBanner(id));
    }

    // Admin từ chối banner
    @PatchMapping("/{id}/reject")
    public ResponseEntity<BannerResponseDTO> rejectBanner(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(bannerService.rejectBanner(id, reason));
    }
}
