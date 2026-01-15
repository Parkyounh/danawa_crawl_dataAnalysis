package com.du.script1.controller;

import com.du.script1.dto.danawaDb.ProductDetailDto;
import com.du.script1.dto.danawaDb.ProductListDto;
import com.du.script1.service.NewProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/new") // 기존 API와 구분하기 위해 /new 경로 추가
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8081")
public class NewProductController {

    private final NewProductService newProductService;

    // 1. 신규 DB 목록 조회 (페이징/필터 포함)
    @GetMapping("/product")
    public ResponseEntity<List<ProductListDto>> getNewProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 35, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(newProductService.getProductList(pageable, category, query));
    }

    // 2. 신규 DB 상세 조회
    @GetMapping("/product/{productCode}")
    public ResponseEntity<ProductDetailDto> getNewProductDetail(@PathVariable String productCode) {
        return ResponseEntity.ok(newProductService.getProductDetail(productCode));
    }
}