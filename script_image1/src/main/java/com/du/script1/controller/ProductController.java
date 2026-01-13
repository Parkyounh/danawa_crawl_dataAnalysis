package com.du.script1.controller;

import com.du.script1.dto.danawaDb.ProductDetailDto;
import com.du.script1.dto.danawaDb.ProductListDto;
import com.du.script1.service.ProductService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<ProductListDto>> getOldProducts() {
        return ResponseEntity.ok(productService.getProductList());
    }

    // 2. 상세 조회
    @GetMapping("/product/{productCode}")
    public ResponseEntity<ProductDetailDto> getProductDetail(@PathVariable String productCode) {
        // 서비스 레이어에서 작성하신 getProductDetail 메서드를 호출합니다.
        return ResponseEntity.ok(productService.getProductDetail(productCode));
    }
}