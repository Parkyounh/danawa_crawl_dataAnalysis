package com.du.script1.repository.danawaNewDb;

import com.du.script1.domain.danawaNewDb.NewPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewPriceHistoryRepository extends JpaRepository<NewPriceHistory, Integer> {
    // 1. 최신 가격 정보 1건 조회 (목록 페이지용)
    NewPriceHistory findFirstByProductIdOrderByCrawledDateDesc(Integer productId);

    // 2. 특정 상품의 모든 가격 이력 조회 (상세 페이지 그래프용)
    List<NewPriceHistory> findByProductIdOrderByCrawledDateDesc(Integer productId);
}