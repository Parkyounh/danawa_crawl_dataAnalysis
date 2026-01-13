package com.du.script1.service;

import com.du.script1.domain.danawaDb.PriceHistory;
import com.du.script1.domain.danawaDb.Products;
import com.du.script1.domain.danawaDb.Reviews;
import com.du.script1.dto.danawaDb.ProductDetailDto;
import com.du.script1.dto.danawaDb.ProductListDto;
import com.du.script1.repository.danawaDb.PriceHistoryRepository;
import com.du.script1.repository.danawaDb.ProductsRepository;
import com.du.script1.repository.danawaDb.ReviewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductsRepository productsRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ReviewsRepository reviewsRepository;

    @Transactional(readOnly = true, transactionManager = "secondaryTransactionManager")
    public List<ProductListDto> getProductList() {
        List<Products> products = productsRepository.findAll();

        return products.stream().map(product -> {
            ProductListDto dto = new ProductListDto();
            dto.setId(product.getId());
            dto.setProductCode(product.getProductCode());
            dto.setName(product.getName());

            // 1. Category ID -> 한글명 변환
            dto.setCategory(getCategoryName(product.getCategoryId()));

            // 2. 새로운 이미지 경로 규칙 적용
            // 규칙: /danawa_db_image/{categoryId}/{productId}.jpg
            // 이미지 URL 앞에 백엔드 주소를 명확히 붙여줍니다.
            String baseUrl = "http://localhost:8083";
            dto.setImageUrl(baseUrl + "/danawa_db_image/" + product.getCategoryId() + "/" + product.getProductCode() + ".jpg");

            // 3. 최신 가격 정보
            PriceHistory latestPrice = priceHistoryRepository.findFirstByProductIdOrderByCrawledDateDesc(product.getId());
            dto.setPrice(latestPrice != null ? latestPrice.getPrice() : 0);

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true, transactionManager = "secondaryTransactionManager")
    public ProductDetailDto getProductDetail(String productCode) {
        // 1. 구 DB의 Products 테이블에서 productCode로 상품 정보 가져오기
        Products product = productsRepository.findByProductCode(productCode)
                .orElseThrow(() -> new RuntimeException("구 DB에서 해당 상품을 찾을 수 없습니다: " + productCode));

        Integer productId = product.getId(); // 구 DB용 내부 ID

        // 2. 가격 이력 리스트 조회 (구 DB)
        List<ProductDetailDto.PriceHistoryDto> priceDtos = priceHistoryRepository
                .findByProductIdOrderByCrawledDateDesc(productId)
                .stream()
                .map(p -> ProductDetailDto.PriceHistoryDto.builder()
                        .date(p.getCrawledDate().toString())
                        .price(p.getPrice())
                        .build())
                .collect(Collectors.toList());

        // 3. 리뷰 리스트 조회 (구 DB)
        List<ProductDetailDto.ReviewDto> reviewDtos = reviewsRepository
                .findByProductId(productId)
                .stream()
                .map(r -> ProductDetailDto.ReviewDto.builder()
                        .content(r.getReviewText())
                        .rating(r.getRating())
                        .platform(r.getPlatform())
                        .build())
                .collect(Collectors.toList());

        // 4. DTO 조립 (신규 DB 스펙 제외)
        return ProductDetailDto.builder()
                .name(product.getName())
                .categoryId(product.getCategoryId())
                .productCode(product.getProductCode())
                .specifications(product.getSpecifications()) // 구 DB Products에도 jsonb가 있다면 유지
                .priceHistory(priceDtos)
                .reviews(reviewDtos)
                .build();
    }

    // 카테고리 매핑용 헬퍼 메서드
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
}
