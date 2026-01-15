package com.du.script1.controller;

import com.du.script1.service.ImageSimilarityService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8081")
public class RagController {

    private final ImageSimilarityService imageSimilarityService;

    @Data
    @NoArgsConstructor // JSON 파싱을 위한 기본 생성자
    @AllArgsConstructor // 모든 필드 생성자
    public static class SimilarImageRequest {
        private String pcode;
        private int top;
        private List<String> filters; // 현재 Payload가 ["123", "456"] 배열이므로 List여야 함
    }

    @PostMapping("/api/similar-images")
    public ResponseEntity<Map<String, Object>> searchSimilarImages(@RequestBody SimilarImageRequest request) {
        log.info("유사 이미지 검색 요청 수신: pcode={}, filters={}", request.getPcode(), request.getFilters());

        Map<String, Object> result = imageSimilarityService.searchSimilarImages(
                request.getPcode(),
                request.getTop(),
                request.getFilters()
        );
        return ResponseEntity.ok(result);
    }
}