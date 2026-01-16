package com.du.script1.repository.danawaDb;

import com.du.script1.domain.danawaDb.Products;
import com.du.script1.domain.danawaNewDb.NewProducts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Integer> {

    // 상품 코드로 찾기
    Optional<Products> findByProductCode(String productCode);

    // 4. 특정 카테고리의 상품들만 findAll 할 때
    List<Products> findByCategoryId(Integer categoryId);

    // 카테고리 ID 리스트(Long/Integer)와 검색어를 이용한 페이징 쿼리
    // 실제 DB 구조에 맞춰 categoryId를 필터링하도록 구성합니다.
    @Query("SELECT p FROM Products p WHERE " +
            "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
            "(:name IS NULL OR p.name LIKE %:name%)")
    Page<Products> findProductsWithFilters(
            @Param("categoryId") Integer categoryId,
            @Param("name") String name,
            Pageable pageable);

    // filter 가져오기
    @Query("SELECT p.specifications FROM Products p WHERE p.categoryId = :categoryId")
    List<Map<String, Object>> findSpecificationsByCategoryId(@Param("categoryId") Integer categoryId);
}
