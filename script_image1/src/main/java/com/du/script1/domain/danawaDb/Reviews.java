package com.du.script1.domain.danawaDb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "review_text", columnDefinition = "text") // 스네이크 케이스 명시
    private String reviewText;

    @Column(name = "platform", length = 100)
    private String platform;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

}