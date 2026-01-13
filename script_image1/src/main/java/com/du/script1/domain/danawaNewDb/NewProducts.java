package com.du.script1.domain.danawaNewDb;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewProducts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(columnDefinition = "text")
    private String name;

    /**
     * PostgreSQL의 jsonb 타입을 Map으로 매핑합니다.
     * hypersistence-utils 라이브러리를 사용하면 JSON 데이터를
     * Java의 Map 객체로 자동 변환해줍니다.
     */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> specifications;

    @Column(length = 255)
    private String manufacturer;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "registered_date")
    private LocalDate registeredDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 생성 시점에 시간을 자동으로 넣어주기 위한 메소드
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 수정 시점에 시간을 자동으로 업데이트하기 위한 메소드
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
