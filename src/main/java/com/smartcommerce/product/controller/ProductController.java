package com.smartcommerce.product.controller;

import com.smartcommerce.product.dto.request.CreateProductRequest;
import com.smartcommerce.product.dto.request.ProductFilterDTO;
import com.smartcommerce.product.dto.request.UpdateProductRequest;
import com.smartcommerce.product.dto.response.ProductResponseDTO;
import com.smartcommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

   @GetMapping("/products")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
           ProductFilterDTO filter,
            Pageable pageable){
       return ResponseEntity.ok(productService.getAllProducts(filter,pageable));
   }

   @PostMapping("/admin/products")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody CreateProductRequest request){

       return ResponseEntity.status(HttpStatus.CREATED)
               .body(productService.createProduct(request));
   }

   @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
       productService.deleteProduct(id);
       return ResponseEntity.noContent().build();
   }

   @PutMapping("/admin/products/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id , @Valid @RequestBody UpdateProductRequest request){
       return ResponseEntity.ok(productService.updateProduct(id,request));
   }

   @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id){
       return  ResponseEntity.ok((productService.getProductById(id)));
   }
}
