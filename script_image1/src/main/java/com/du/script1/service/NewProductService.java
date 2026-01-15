package com.du.script1.service;

import com.du.script1.domain.danawaNewDb.*;
import com.du.script1.dto.danawaDb.ProductDetailDto;
import com.du.script1.dto.danawaDb.ProductListDto;
import com.du.script1.repository.danawaNewDb.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewProductService {

    private final NewProductsRepository newProductsRepository;
    private final NewPriceHistoryRepository newPriceHistoryRepository;
    private final NewReviewsRepository newReviewsRepository;

    @Transactional(readOnly = true) // 신규 DB는 메인 DB(Primary)일 가능성이 높으므로 매니저 생략 가능
    public List<ProductListDto> getProductList(Pageable pageable, String categoryName, String query) {
        Integer categoryId = getCategoryIdFromName(categoryName);

        // NewProductsRepository에 findProductsWithFilters 메서드가 정의되어 있어야 함
        Page<NewProducts> productPage = newProductsRepository.findProductsWithFilters(categoryId, query, pageable);

        return productPage.getContent().stream().map(product -> {
            ProductListDto dto = new ProductListDto();
            dto.setId(product.getId());
            dto.setProductCode(product.getProductCode());
            dto.setName(product.getName());
            dto.setCategory(getCategoryName(product.getCategoryId()));

            String baseUrl = "http://localhost:8083";
            dto.setImageUrl(baseUrl + "/danawa_new_db_image/" + product.getCategoryId() + "/" + product.getProductCode() + ".jpg");

            NewPriceHistory latestPrice = newPriceHistoryRepository.findFirstByProductIdOrderByCrawledDateDesc(product.getId());
            dto.setPrice(latestPrice != null ? latestPrice.getPrice() : 0);

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDetailDto getProductDetail(String productCode) {
        NewProducts product = newProductsRepository.findByProductCode(productCode)
                .orElseThrow(() -> new RuntimeException("신규 DB에서 해당 상품을 찾을 수 없습니다: " + productCode));

        Integer productId = product.getId();

        String baseUrl = "http://localhost:8083";

//        List<ProductDetailDto.PriceHistoryDto> priceDtos = newPriceHistoryRepository
//                .findByProductIdOrderByCrawledDateDesc(productId)
//                .stream()
//                .map(p -> ProductDetailDto.PriceHistoryDto.builder()
//                        .date(p.getCrawledDate().toString())
//                        .price(p.getPrice())
//                        .build())
//                .collect(Collectors.toList());

        List<ProductDetailDto.ReviewDto> reviewDtos = newReviewsRepository
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
//                .priceHistory(priceDtos)
                .reviews(reviewDtos)
                .imageUrl(baseUrl + "/danawa_new_db_image/" + product.getCategoryId() + "/" + product.getProductCode() + ".jpg")
                .build();
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