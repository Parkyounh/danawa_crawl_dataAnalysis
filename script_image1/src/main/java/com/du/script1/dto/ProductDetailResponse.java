//package com.du.script1.dto;
//
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.Map;
//
//@Getter
//@Setter
//public class ProductDetailResponse {
//    private Integer id;
//    private String productCode;
//    private String name;
//    private Map<String, Object> specifications; // JSONB 데이터 전체
//    private String manufacturer;
//
//    // 연관 데이터 리스트
//    private List<PriceHistoryDto> priceHistories; // 가격 변동 이력
//    private List<ReviewDto> reviews;              // 사용자 리뷰들
//    private String imageUrl;                      // 대표 이미지
//}