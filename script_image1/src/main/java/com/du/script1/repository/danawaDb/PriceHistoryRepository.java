package com.du.script1.repository.danawaDb;

import com.du.script1.domain.danawaDb.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {

    // [목록용] 특정 상품의 가장 최근 가격 딱 하나만 가져오기
    PriceHistory findFirstByProductIdOrderByCrawledDateDesc(Integer productId);

    // [상세용] 특정 상품의 모든 가격 이력을 날짜 역순으로 가져오기
    List<PriceHistory> findByProductIdOrderByCrawledDateDesc(Integer productId);
}