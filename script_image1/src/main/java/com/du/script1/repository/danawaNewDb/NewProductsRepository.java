package com.du.script1.repository.danawaNewDb;

import com.du.script1.domain.danawaNewDb.NewProducts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewProductsRepository extends JpaRepository<NewProducts, Integer> {

    // 상품 코드로 찾기
    Optional<NewProducts> findByProductCode(String productCode);

    // 4. 특정 카테고리의 상품들만 findAll 할 때
    List<NewProducts> findByCategoryId(Integer categoryId);

    // 1. 카테고리 ID 및 이름 검색 필터링 (기존 로직 동일)
    @Query("SELECT p FROM NewProducts p WHERE " +
            "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
            "(:name IS NULL OR p.name LIKE %:name%)")
    Page<NewProducts> findProductsWithFilters(
            @Param("categoryId") Integer categoryId,
            @Param("name") String name,
            Pageable pageable);

}
