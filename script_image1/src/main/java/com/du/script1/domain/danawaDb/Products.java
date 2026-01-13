package com.du.script1.domain.danawaDb;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "name", columnDefinition = "text")
    private String name;

    @Type(JsonBinaryType.class)
    @Column(name = "specifications", columnDefinition = "jsonb")
    private Map<String, Object> specifications;

    @Column(name = "manufacturer", length = 255) // 명시적 지정
    private String manufacturer;

    @Column(name = "category_id")
    private Integer categoryId;


}