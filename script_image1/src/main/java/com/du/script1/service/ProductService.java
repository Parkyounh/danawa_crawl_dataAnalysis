package com.du.script1.service;

import com.du.script1.domain.danawaDb.PriceHistory;
import com.du.script1.domain.danawaDb.Products;
import com.du.script1.domain.danawaDb.Reviews;
import com.du.script1.dto.danawaDb.ProductDetailDto;
import com.du.script1.dto.danawaDb.ProductListDto;
import com.du.script1.repository.danawaDb.PriceHistoryRepository;
import com.du.script1.repository.danawaDb.ProductsRepository;
import com.du.script1.repository.danawaDb.ReviewsRepository;
import com.fasterxml.jackson.databind.ObjectMapper; // 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그용 추가
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductsRepository productsRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ReviewsRepository reviewsRepository;
    private final ObjectMapper objectMapper; // 주입 추가

    @Transactional(readOnly = true, transactionManager = "secondaryTransactionManager")
    public List<ProductListDto> getProductList(Pageable pageable, String categoryName, String query) {

        // 1. 한글명을 숫자 ID로 변환
        Integer categoryId = getCategoryIdFromName(categoryName);

        // 2. 필터링된 상품 페이지 조회
        Page<Products> productPage = productsRepository.findProductsWithFilters(categoryId, query, pageable);

        return productPage.getContent().stream().map(product -> {
            ProductListDto dto = new ProductListDto();
            dto.setId(product.getId());
            dto.setProductCode(product.getProductCode());
            dto.setName(product.getName());
            dto.setCategory(getCategoryName(product.getCategoryId()));
            dto.setCategoryId(product.getCategoryId()); // 카테고리 ID 세팅

            // 새로운 이미지 경로 규칙 적용
            String baseUrl = "http://localhost:8083";
            dto.setImageUrl(baseUrl + "/danawa_db_image/" + product.getCategoryId() + "/" + product.getProductCode() + ".jpg");

            // 최신 가격 정보
            PriceHistory latestPrice = priceHistoryRepository.findFirstByProductIdOrderByCrawledDateDesc(product.getId());
            dto.setPrice(latestPrice != null ? latestPrice.getPrice() : 0);

            // --- [추가 내용] 스펙 정보 JSON 문자열 변환 ---
            try {
                if (product.getSpecifications() != null) {
                    dto.setSpecifications(objectMapper.writeValueAsString(product.getSpecifications()));
                }
            } catch (Exception e) {
                log.error("스펙 JSON 변환 에러 (ProductCode: {}): {}", product.getProductCode(), e.getMessage());
            }
            // ------------------------------------------

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true, transactionManager = "secondaryTransactionManager")
    public ProductDetailDto getProductDetail(String productCode) {
        Products product = productsRepository.findByProductCode(productCode)
                .orElseThrow(() -> new RuntimeException("구 DB에서 해당 상품을 찾을 수 없습니다: " + productCode));

        Integer productId = product.getId();
        String baseUrl = "http://localhost:8083";

        List<ProductDetailDto.PriceHistoryDto> priceDtos = priceHistoryRepository
                .findByProductIdOrderByCrawledDateDesc(productId)
                .stream()
                .map(p -> ProductDetailDto.PriceHistoryDto.builder()
                        .date(p.getCrawledDate().toString())
                        .price(p.getPrice())
                        .build())
                .collect(Collectors.toList());

        List<ProductDetailDto.ReviewDto> reviewDtos = reviewsRepository
                .findByProductId(productId)
                .stream()
                .map(r -> ProductDetailDto.ReviewDto.builder()
                        .content(r.getReviewText())
                        .rating(r.getRating())
                        .platform(r.getPlatform())
                        .build())
                .collect(Collectors.toList());

        return ProductDetailDto.builder()
                .name(product.getName())
                .categoryId(product.getCategoryId())
                .productCode(product.getProductCode())
                .specifications(product.getSpecifications())
                .priceHistory(priceDtos)
                .reviews(reviewDtos)
                .imageUrl(baseUrl + "/danawa_db_image/" + product.getCategoryId() + "/" + product.getProductCode() + ".jpg")
                .build();
    }

    private String getCategoryName(Integer categoryId) {
        if (categoryId == null) return "미분류";
        return switch (categoryId) {
            case 112756 -> "데스크탑";
            case 15236036 -> "소파";
            case 18234911 -> "패딩";
            case 18242355 -> "신발";
            default -> "기타(" + categoryId + ")";
        };
    }

    private Integer getCategoryIdFromName(String name) {
        if (name == null || name.isEmpty()) return null;
        return switch (name) {
            case "데스크탑" -> 112756;
            case "소파" -> 15236036;
            case "패딩" -> 18234911;
            case "신발" -> 18242355;
            default -> null;
        };
    }
}