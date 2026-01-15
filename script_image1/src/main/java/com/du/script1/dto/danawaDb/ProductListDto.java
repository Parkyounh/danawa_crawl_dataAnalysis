package com.du.script1.dto.danawaDb;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductListDto {
    private Integer id;
    private String productCode;
    private String name;
    private String category;
    private Integer categoryId; // 카테고리 ID 추가
    // private String manufacturer;
    private Integer price; // 신규 DB의 최신 가격
    private String imageUrl;
    private String specifications; // 필터 생성을 위해 스펙 정보 추가
}