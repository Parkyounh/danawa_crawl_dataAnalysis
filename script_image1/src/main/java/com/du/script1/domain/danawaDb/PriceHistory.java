package com.du.script1.domain.danawaDb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "price")
    private Integer price;

//    @Column(name = "unit_price")
//    private String unitPrice;

    @Column(name = "crawled_date")
    private LocalDate crawledDate;

}