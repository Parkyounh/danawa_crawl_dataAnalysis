package com.du.script1.controller;

import com.du.script1.dto.danawaDb.CategorySpecDto;
import com.du.script1.dto.danawaDb.ProductDetailDto;
import com.du.script1.dto.danawaDb.ProductListDto;
import com.du.script1.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8081") // CORS 허용 추가
public class ProductController {

    private final ProductService productService;

    // 1. 목록 조회
    @GetMapping("/product")
    public ResponseEntity<List<ProductListDto>> getOldProducts(
            @RequestParam(required = false) String category, // 프론트의 params.category와 매칭
            @RequestParam(required = false) String query,    // 프론트의 params.query와 매칭
            @PageableDefault(size = 35, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(productService.getProductList(pageable, category, query));
    }

    // 2. 상세 조회
    @GetMapping("/product/{productCode}")
    public ResponseEntity<ProductDetailDto> getProductDetail(@PathVariable String productCode) {
        // 서비스 레이어에서 작성하신 getProductDetail 메서드를 호출합니다.
        return ResponseEntity.ok(productService.getProductDetail(productCode));
    }

    @GetMapping("/category/specs")
    public ResponseEntity<CategorySpecDto> getCategorySpecs(@RequestParam String category) {
        CategorySpecDto specDto = productService.getCategorySpecFilters(category);
        if (specDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(specDto);
    }
}