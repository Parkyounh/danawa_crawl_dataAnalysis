package com.du.script1.repository.danawaNewDb;

import com.du.script1.domain.danawaNewDb.NewTargetLinks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewTargetLinksRepository extends JpaRepository<NewTargetLinks, Integer> {
    // 특정 상품 코드로 링크 찾기
    List<NewTargetLinks> findByProductCode(String productCode);

    // 특정 카테고리의 모든 링크 찾기
    List<NewTargetLinks> findByCategoryId(Integer categoryId);
}