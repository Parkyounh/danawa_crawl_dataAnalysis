package com.du.script1.domain.danawaNewDb;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "target_links")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class NewTargetLinks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}