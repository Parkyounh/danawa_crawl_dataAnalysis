package com.du.script1.dto.danawaDb;

import lombok.*;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySpecDto {
    private Integer categoryId;
    private String categoryName;
    // Key: 스펙 항목명(예: "커버 재질"), Value: 중복 없는 값들의 집합(예: ["천연가죽", "기능성패브릭"])
    private Map<String, Set<String>> specs;
}