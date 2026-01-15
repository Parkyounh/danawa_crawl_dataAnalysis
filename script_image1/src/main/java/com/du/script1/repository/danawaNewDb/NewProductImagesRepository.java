package com.du.script1.repository.danawaNewDb;

import com.du.script1.domain.danawaNewDb.NewProductImages;
import com.du.script1.domain.danawaNewDb.NewProducts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NewProductImagesRepository extends JpaRepository<NewProductImages, Integer> {

    // 크롤링 성공 여부에 따른 이미지 목록
    java.util.List<NewProductImages> findBySuccess(Boolean success);


}