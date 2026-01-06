
package com.example.WorkWite_Repo_BE.dtos.BannerDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO cho việc TẠO banner mới
 * Tất cả field đều BẮT BUỘC
 */
@Data
public class BannerRequestDTO {
    
    @NotBlank(message = "Tên công ty không được để trống")
    private String companyName;
    
    @NotBlank(message = "Email công ty không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String companyEmail;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải là 10-11 chữ số")
    private String companyPhone;

    // Thông tin banner
    private String bannerImage;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;
    
    @NotBlank(message = "Loại banner không được để trống")
    @Pattern(regexp = "(?i)(Vip|Featured|Standard)", message = "Loại banner phải là Vip, Featured hoặc Standard")
    private String bannerType;
}
