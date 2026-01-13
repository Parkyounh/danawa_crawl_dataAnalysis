package com.du.script1.repository.danawaNewDb;

import com.du.script1.domain.danawaNewDb.NewProductImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NewProductImagesRepository extends JpaRepository<NewProductImages, Integer> {
    // 상품 코드로 이미지 URL 찾기
    Optional<NewProductImages> findByProductCode(String productCode);

    // 크롤링 성공 여부에 따른 이미지 목록
    java.util.List<NewProductImages> findBySuccess(Boolean success);
}