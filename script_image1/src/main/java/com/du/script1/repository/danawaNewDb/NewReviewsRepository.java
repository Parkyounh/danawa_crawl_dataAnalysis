package com.du.script1.repository.danawaNewDb;

import com.du.script1.domain.danawaNewDb.NewReviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewReviewsRepository extends JpaRepository<NewReviews, Integer> {
    // 특정 상품의 모든 리뷰 가져오기 (product_id 기준)
    List<NewReviews> findByProductId(Integer productId);

    // 평점(rating)이 특정 점수 이상인 리뷰만 가져오기
    List<NewReviews> findByProductIdAndRatingGreaterThanEqual(Integer productId, Integer rating);
}