package com.du.script1.dto.danawaDb;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ProductDetailDto {
    // 기본 정보
    private String name;
    private String productCode;
    private Integer categoryId; // 카테고리 ID 추가
    private String imageUrl;
    private Map<String, Object> specifications; // JSONB 데이터

    // 신규/구 DB에서 모은 리스트 데이터
    private List<PriceHistoryDto> priceHistory;
    private List<ReviewDto> reviews;

    @Getter
    @Builder
    public static class PriceHistoryDto {
        private String date;
        private Integer price;
    }

    @Getter
    @Builder
    public static class ReviewDto {
        private String content;
        private Integer rating;
        private String platform;
    }
}