package com.du.script1.repository.danawaNewDb;

import com.du.script1.domain.danawaNewDb.NewPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewPriceHistoryRepository extends JpaRepository<NewPriceHistory, Integer> {
    // 특정 상품의 가격 변동 이력을 날짜순으로 가져오기
    List<NewPriceHistory> findByProductIdOrderByCrawledDateDesc(Integer productId);
}