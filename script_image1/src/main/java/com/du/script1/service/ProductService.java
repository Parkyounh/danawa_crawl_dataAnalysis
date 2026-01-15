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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<ProductListDto> getProductList(Pageable pageable, String categoryName, String query) {

        // 로그를 찍어 프론트에서 어떤 값이 넘어오는지 확인해보세요.
        // System.out.println("넘어온 카테고리명: " + categoryName);

        // 1. 한글명을 숫자 ID로 변환
        Integer categoryId = getCategoryIdFromName(categoryName);

        // 2. 변환된 ID 확인 (데스크탑 클릭 시 112756이 나와야 함)
        // System.out.println("변환된 카테고리 ID: " + categoryId);

        Page<Products> productPage = productsRepository.findProductsWithFilters(categoryId, query, pageable);

        return productPage.getContent().stream().map(product -> {
            ProductListDto dto = new ProductListDto();
            dto.setId(product.getId());
            dto.setProductCode(product.getProductCode());
            dto.setName(product.getName());
            dto.setCategory(getCategoryName(product.getCategoryId()));

            String baseUrl = "http://localhost:8083";
            dto.setImageUrl(baseUrl + "/danawa_db_image/" + product.getCategoryId() + "/" + product.getProductCode() + ".jpg");

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

        String baseUrl = "http://localhost:8083";

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
                .imageUrl(baseUrl + "/danawa_db_image/" + product.getCategoryId() + "/" + product.getProductCode() + ".jpg")
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

    // 역변환 메서드 추가
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
