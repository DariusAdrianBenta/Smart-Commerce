package com.smartcommerce.product.entity;

import com.smartcommerce.category.entity.Category;
import com.smartcommerce.productimage.entity.ProductImage;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // true solo si el producto fue ocultado por la cascada de su categoría.
    // Permite restaurar SOLO estos al volver a mostrar la categoría.
    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean hiddenByCategory = false;

    private String brand;

    private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();


// Se ejecuta automáticamente antes de guardar en la base de datos.
// Sirve para asignar valores por defecto, como la fecha de creación.
    @PrePersist
    public void prePersist(){
        this.createdAt=LocalDateTime.now();
    }







}
