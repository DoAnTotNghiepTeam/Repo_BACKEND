package com.example.WorkWite_Repo_BE.controlers;

import com.example.WorkWite_Repo_BE.dtos.JobScore.RecommendationResponseDto;
import com.example.WorkWite_Repo_BE.services.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

//import static com.example.WorkWite_Repo_BE.exceptions.GlobalExceptionHandler.log;

//@RestController
//@RequestMapping("/api/recommend")
//@RequiredArgsConstructor
//public class RecommendationController {
//    private final RecommendationService recommendationService;
//
//    /**
//     * API đề xuất công việc cho 1 ứng viên
//     *
//     * @param candidateId id ứng viên
//     * @param limit số lượng job muốn lấy (default = 10)
//     * @param minScore điểm tối thiểu (default = 50)
//     */
//    @GetMapping("/{candidateId}")
//    public ResponseEntity<RecommendationResponseDto> recommendJobs(
//            @PathVariable Long candidateId,
//            @RequestParam(defaultValue = "10") int limit,
//            @RequestParam(defaultValue = "50") double minScore
//    ) {
//        RecommendationResponseDto response =
//                recommendationService.recommendForCandidate(candidateId, limit, minScore);
//        return ResponseEntity.ok(response);
//    }
//}

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // nếu FE chạy port khác thì rất cần
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * API đề xuất công việc cho 1 ứng viên
     *
     * @param candidateId id ứng viên
     * @param limit số lượng job muốn lấy (default = 10)
     * @param minScore điểm tối thiểu (default = 50)
     */
    @GetMapping("/{candidateId}")
    public ResponseEntity<?> recommendJobs(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "50") double minScore
    ) {
        try {
            log.info("Recommendation API called for candidateId = {}", candidateId);

            RecommendationResponseDto response =
                    recommendationService.recommendForCandidate(candidateId, limit, minScore);

            return ResponseEntity.ok(response);

        } catch (NoSuchElementException ex) {
            log.error("Resume not found: {}", ex.getMessage());
            return ResponseEntity.status(404).body(
                    new ErrorMessage("Resume not found for candidateId " + candidateId)
            );

        } catch (Exception ex) {
            log.error("Error recommending jobs", ex);
            return ResponseEntity.status(500).body(
                    new ErrorMessage("Internal Server Error: " + ex.getMessage())
            );
        }
    }

    record ErrorMessage(String message) {}
}
