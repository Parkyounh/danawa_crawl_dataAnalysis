package com.du.script1.repository.danawaDb;

import com.du.script1.domain.danawaDb.Reviews;
import com.du.script1.domain.danawaNewDb.NewReviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewsRepository extends JpaRepository<Reviews, Integer> {
    // 특정 상품의 모든 리뷰 가져오기 (product_id 기준)
    List<Reviews> findByProductId(Integer productId);
}