package com.smartcommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;


    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    private String brand;

    private LocalDateTime createdAt;

// ? DESCOMENTAR   @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "category_id")
//    private Category category;


// La relación está gestionada desde la entidad ProductImage.
// ? DESCOMENTAR   @OneToMany(mappedBy = "product",cascade = CascadeType.ALL,orphanRemoval = true)
//    private List<ProductImage> images = new ArrayList<>();


// Se ejecuta automáticamente antes de guardar en la base de datos.
// Sirve para asignar valores por defecto, como la fecha de creación.
    @PrePersist
    public void prePersist(){
        this.createdAt=LocalDateTime.now();
    }







}
