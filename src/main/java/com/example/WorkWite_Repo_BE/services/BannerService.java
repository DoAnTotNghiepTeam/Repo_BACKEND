package com.example.WorkWite_Repo_BE.services;

import com.example.WorkWite_Repo_BE.dtos.BannerDto.BannerRequestDTO;
import com.example.WorkWite_Repo_BE.dtos.BannerDto.BannerUpdateRequestDTO;
import com.example.WorkWite_Repo_BE.dtos.BannerDto.BannerResponseDTO;
import com.example.WorkWite_Repo_BE.dtos.BannerDto.PaginatedBannerResponseDto;
import com.example.WorkWite_Repo_BE.entities.Banner;
import com.example.WorkWite_Repo_BE.entities.User;
import com.example.WorkWite_Repo_BE.enums.BannerStatus;
import com.example.WorkWite_Repo_BE.repositories.BannerRepository;
import com.example.WorkWite_Repo_BE.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final UserJpaRepository userJpaRepository;


    //    convert to dto
    private BannerResponseDTO toDTO(Banner banner) {
        BannerResponseDTO dto = new BannerResponseDTO();
        dto.setId(banner.getId());
        dto.setBannerType(banner.getBannerType());
        dto.setCompanyName(banner.getCompanyName());
        dto.setCompanyEmail(banner.getCompanyEmail());
        dto.setBannerImage(banner.getBannerImage());
        dto.setStartDate(banner.getStartDate());
        dto.setEndDate(banner.getEndDate());
        dto.setAmount(banner.getAmount());
        dto.setStatus(banner.getStatus() != null ? banner.getStatus().name() : null);
        dto.setCreatedAt(banner.getCreatedAt());
        dto.setUpdatedAt(banner.getUpdatedAt());

        if (banner.getUser() != null) {
            dto.setUserId(banner.getUser().getId());
            dto.setUserName(banner.getUser().getFullName());
        }

        return dto;
    }

    // Trả về danh sách BannerResponseDTO cho các banner ACTIVE
    // Nếu có bannerType -> lọc theo type, không có -> trả tất cả
    public List<BannerResponseDTO> getActiveBannerList(String bannerType) {
        List<Banner> activeBanners = getBannersByStatus(BannerStatus.ACTIVE);
        
        // Lọc theo bannerType nếu có
        if (bannerType != null && !bannerType.trim().isEmpty()) {
            activeBanners = activeBanners.stream()
                    .filter(b -> b.getBannerType().equalsIgnoreCase(bannerType))
                    .collect(Collectors.toList());
        }
        
        return activeBanners.stream()
                .map(b -> {
                    BannerResponseDTO dto = new BannerResponseDTO();
                    dto.setCompanyName(b.getCompanyName());
                    dto.setStartDate(b.getStartDate());
                    dto.setEndDate(b.getEndDate());
                    dto.setStatus(b.getStatus() != null ? b.getStatus().name() : null);
                    dto.setBannerImage(b.getBannerImage());
                    dto.setBannerType(b.getBannerType());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // Lấy danh sách banner đã được admin DUYỆT (APPROVED) - chờ đến ngày hiển thị
    public List<BannerResponseDTO> getApprovedBanners() {
        List<Banner> approvedBanners = getBannersByStatus(BannerStatus.APPROVED);
        return approvedBanners.stream()
                .map(b -> {
                    BannerResponseDTO dto = new BannerResponseDTO();
                    dto.setCompanyName(b.getCompanyName());
                    dto.setStartDate(b.getStartDate());
                    dto.setEndDate(b.getEndDate());
                    dto.setStatus(b.getStatus() != null ? b.getStatus().name() : null);
                    dto.setBannerImage(b.getBannerImage());
                    dto.setBannerType(b.getBannerType());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Kiểm tra và kích hoạt các banner có startDate <= hiện tại và đang ở trạng thái PENDING
     */

public void activateBannersIfNeeded() {
    LocalDateTime now = LocalDateTime.now();
    
    // Chỉ query banner cần activate
    List<Banner> pendingBanners = bannerRepository.findByStatusAndStartDateBefore(
        BannerStatus.PENDING, now
    );
    
    if (!pendingBanners.isEmpty()) {
        pendingBanners.forEach(banner -> {
            banner.setStatus(BannerStatus.ACTIVE);
            banner.setUpdatedAt(now);
        });
        
        bannerRepository.saveAll(pendingBanners);  // ← Batch update (1 query)
    }
}


    // ==========================
    // GET Methods
    // ==========================

    public PaginatedBannerResponseDto getAllBannersPaginated(int page, int size) {
        int pageNumber = Math.max(page - 1, 0);
        var pageable = org.springframework.data.domain.PageRequest.of(pageNumber, size);
        var bannerPage = bannerRepository.findAll(pageable);

        List<BannerResponseDTO> bannerDtos = bannerPage.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PaginatedBannerResponseDto dto = new PaginatedBannerResponseDto();
        dto.setData(bannerDtos);
        dto.setPageNumber(bannerPage.getNumber() + 1);
        dto.setPageSize(bannerPage.getSize());
        dto.setTotalRecords(bannerPage.getTotalElements());
        dto.setTotalPages(bannerPage.getTotalPages());
        dto.setHasNext(bannerPage.hasNext());
        dto.setHasPrevious(bannerPage.hasPrevious());

        return dto;
    }

    public List<BannerResponseDTO> getBannersByUserId(Long userId) {
        return bannerRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<Banner> getBannersByStatus(BannerStatus status) {
        return bannerRepository.findByStatus(status);
    }

    public List<BannerResponseDTO> getActiveBannersByType(String bannerType) {
        return bannerRepository.findByBannerTypeAndStatus(bannerType, BannerStatus.ACTIVE)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BannerResponseDTO> getAllBanners() {
        return bannerRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==========================
    // Business Logic
    // ==========================

    @Transactional
    public void expireBannersIfNeeded() {
        LocalDateTime now = LocalDateTime.now();
        
        List<Banner> activeBanners = bannerRepository.findByStatusAndEndDateBefore(
            BannerStatus.ACTIVE, now
        );
        
        if (!activeBanners.isEmpty()) {
            activeBanners.forEach(banner -> {
                banner.setStatus(BannerStatus.EXPIRED);
                banner.setUpdatedAt(now);
            });
            
            bannerRepository.saveAll(activeBanners);
        }
    }

    @Transactional
    public BannerResponseDTO approveBanner(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));
        LocalDateTime now = LocalDateTime.now();
        if (banner.getStartDate() != null && now.isBefore(banner.getStartDate())) {
            // Nếu ngày bắt đầu là tương lai, set trạng thái APPROVED (hoặc WAITING)
            banner.setStatus(BannerStatus.APPROVED); // Nếu chưa có enum APPROVED thì thêm vào enums/BannerStatus.java
        } else {
            // Nếu ngày bắt đầu <= hiện tại, set ACTIVE
            banner.setStatus(BannerStatus.ACTIVE);
        }
        banner.setUpdatedAt(now);
        Banner saved = bannerRepository.save(banner);
        return toDTO(saved);
    }

    @Transactional
    public BannerResponseDTO rejectBanner(Long id, String reason) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        banner.setStatus(BannerStatus.REJECTED);
        banner.setUpdatedAt(LocalDateTime.now());

        // Hoàn tiền cho user nếu banner bị từ chối
        User user = banner.getUser();
        if (user != null && banner.getAmount() != null) {
            user.setBalance(user.getBalance() + banner.getAmount());
            userJpaRepository.save(user);
        }

        Banner saved = bannerRepository.save(banner);
        return toDTO(saved);
    }

    @Transactional
    public BannerResponseDTO createBanner(BannerRequestDTO requestDTO) {
        // Annotation validation đã xử lý: companyName, companyEmail, companyPhone, bannerType
        
        LocalDate start = requestDTO.getStartDate();
        LocalDate end = requestDTO.getEndDate();

        // Validate startDate phải >= hôm nay
        if (start.isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày bắt đầu phải từ hôm nay trở đi");
        }
        
        // Validate end date > start date
        if (end.isBefore(start)) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        // Validate độ dài booking
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        if (days < 1) {
            throw new RuntimeException("Phải thuê ít nhất 1 ngày");
        }
        if (days > 365) {
            throw new RuntimeException("Không thể thuê quá 365 ngày");
        }
        
        // Validate banner image
        if (requestDTO.getBannerImage() == null || requestDTO.getBannerImage().isEmpty()) {
            throw new RuntimeException("Banner image is required");
        }

        // Lấy user từ JWT
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        User user = username != null ? userJpaRepository.findByUsername(username).orElse(null) : null;
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Type đã validate bởi @Pattern annotation
        String type = requestDTO.getBannerType();

        // LOGIC MỚI: Kiểm tra slot đã có người book chưa (BẤT KỊ user nào)
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        
        List<Banner> activeBannersInSlot = bannerRepository.findActiveOrPendingByType(type);
        
        boolean slotOccupied = activeBannersInSlot.stream()
            .anyMatch(b -> b.getStartDate() != null && b.getEndDate() != null
                && !endDateTime.isBefore(b.getStartDate()) 
                && !startDateTime.isAfter(b.getEndDate())
            );
        
        if (slotOccupied) {
            throw new RuntimeException("Slot " + type + " đã có người thuê trong khoảng thời gian này. Vui lòng chọn thời gian khác.");
        }

        // Xác định giá theo loại banner
        final long USD_TO_VND = 26410;
        long pricePerDay = getPricePerDay(type, USD_TO_VND);
        long totalPrice = pricePerDay * days;

        // Kiểm tra số dư
        if (user.getBalance() == null || user.getBalance() < totalPrice) {
            throw new RuntimeException("Số dư không đủ để thuê banner. Cần: " + totalPrice + " VND");
        }

        // Trừ tiền
        user.setBalance(user.getBalance() - totalPrice);
        userJpaRepository.save(user);

        // Tạo banner mới
        Banner banner = new Banner();
        banner.setCompanyName(requestDTO.getCompanyName());
        banner.setCompanyEmail(requestDTO.getCompanyEmail());
        banner.setCompanyPhone(requestDTO.getCompanyPhone());
        banner.setBannerImage(requestDTO.getBannerImage());
        banner.setStartDate(startDateTime);
        banner.setEndDate(endDateTime);
        banner.setAmount(totalPrice);
        banner.setBannerType(type);
        banner.setStatus(BannerStatus.PENDING);
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        banner.setUser(user);

        Banner saved = bannerRepository.save(banner);
        return toDTO(saved);
    }

    // ==========================
    // CRUD
    // ==========================

    public void deleteBanner(Long id) {
        bannerRepository.deleteById(id);
    }
    
    // ==========================
    // Helper Methods
    // ==========================
    
    private long getPricePerDay(String bannerType, long usdToVnd) {
        return switch (bannerType.toLowerCase()) {
            case "vip" -> 3 * usdToVnd;
            case "featured" -> 2 * usdToVnd;
            default -> 1 * usdToVnd;
        };
    }

    @Transactional
    public BannerResponseDTO updateBanner(Long id, BannerUpdateRequestDTO requestDTO) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));
        
        // Chỉ cho phép update banner PENDING hoặc APPROVED (chưa active)
        if (banner.getStatus() == BannerStatus.ACTIVE 
            || banner.getStatus() == BannerStatus.EXPIRED
            || banner.getStatus() == BannerStatus.REJECTED) {
            throw new RuntimeException("Không thể sửa banner đã active/expired/rejected");
        }
        
        // Annotation validation đã xử lý: companyName, companyEmail, phone (optional), bannerType
        
        LocalDate newStart = requestDTO.getStartDate();
        LocalDate newEnd = requestDTO.getEndDate();
        String newType = requestDTO.getBannerType();
        
        // Validate startDate phải >= hôm nay
        if (newStart.isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày bắt đầu phải từ hôm nay trở đi");
        }
        
        // Validate end date > start date
        if (newEnd.isBefore(newStart)) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        // Type đã validate bởi @Pattern annotation
        
        // Tính tiền cũ và mới
        final long USD_TO_VND = 26410;
        long oldDays = ChronoUnit.DAYS.between(
            banner.getStartDate().toLocalDate(), 
            banner.getEndDate().toLocalDate()
        ) + 1;
        long oldPricePerDay = getPricePerDay(banner.getBannerType(), USD_TO_VND);
        long oldTotal = oldPricePerDay * oldDays;
        
        long newDays = ChronoUnit.DAYS.between(newStart, newEnd) + 1;
        if (newDays < 1) {
            throw new RuntimeException("Phải thuê ít nhất 1 ngày");
        }
        if (newDays > 365) {
            throw new RuntimeException("Không thể thuê quá 365 ngày");
        }
        
        long newPricePerDay = getPricePerDay(newType, USD_TO_VND);
        long newTotal = newPricePerDay * newDays;
        
        User user = banner.getUser();
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        long difference = newTotal - oldTotal;
        
        if (difference > 0) {
            // Cần trả thêm tiền
            if (user.getBalance() == null || user.getBalance() < difference) {
                throw new RuntimeException("Số dư không đủ để update banner. Cần thêm: " + difference + " VND");
            }
            user.setBalance(user.getBalance() - difference);
            banner.setAmount(newTotal);
        } else if (difference < 0) {
            // Hoàn lại tiền
            user.setBalance(user.getBalance() + Math.abs(difference));
            banner.setAmount(newTotal);
        }
        
        userJpaRepository.save(user);
        
        // Kiểm tra trùng slot (nếu thay đổi thời gian hoặc type)
        if (!newStart.equals(banner.getStartDate().toLocalDate()) 
            || !newEnd.equals(banner.getEndDate().toLocalDate())
            || !newType.equalsIgnoreCase(banner.getBannerType())) {
            
            LocalDateTime startDateTime = newStart.atStartOfDay();
            LocalDateTime endDateTime = newEnd.atTime(23, 59, 59);
            
            List<Banner> activeBannersInSlot = bannerRepository.findActiveOrPendingByType(newType);
            
            boolean slotOccupied = activeBannersInSlot.stream()
                .filter(b -> !b.getId().equals(id))  // Bỏ qua chính banner này
                .anyMatch(b -> b.getStartDate() != null && b.getEndDate() != null
                    && !endDateTime.isBefore(b.getStartDate()) 
                    && !startDateTime.isAfter(b.getEndDate())
                );
            
            if (slotOccupied) {
                throw new RuntimeException("Slot đã có người thuê trong thời gian mới");
            }
        }
        
        // Update thông tin
        banner.setCompanyName(requestDTO.getCompanyName());
        banner.setCompanyEmail(requestDTO.getCompanyEmail());
        
        // Phone optional - chỉ update nếu có giá trị
        if (requestDTO.getCompanyPhone() != null && !requestDTO.getCompanyPhone().trim().isEmpty()) {
            banner.setCompanyPhone(requestDTO.getCompanyPhone());
        }
        // Nếu null hoặc empty → giữ nguyên phone cũ
        
        // Banner image optional - chỉ update nếu có
        if (requestDTO.getBannerImage() != null && !requestDTO.getBannerImage().isEmpty()) {
            banner.setBannerImage(requestDTO.getBannerImage());
        }
        banner.setStartDate(newStart.atStartOfDay());
        banner.setEndDate(newEnd.atTime(23, 59, 59));
        banner.setBannerType(newType);
        banner.setStatus(BannerStatus.PENDING);
        banner.setUpdatedAt(LocalDateTime.now());
        
        Banner saved = bannerRepository.save(banner);
        return toDTO(saved);
    }



}

