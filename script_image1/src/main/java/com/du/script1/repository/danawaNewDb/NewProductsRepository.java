package com.du.script1.repository.danawaNewDb;

import com.du.script1.domain.danawaNewDb.NewProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewProductsRepository extends JpaRepository<NewProducts, Integer> {

    // 상품 코드로 찾기
    Optional<NewProducts> findByProductCode(String productCode);

    // 4. 특정 카테고리의 상품들만 findAll 할 때
    List<NewProducts> findByCategoryId(Integer categoryId);
}
