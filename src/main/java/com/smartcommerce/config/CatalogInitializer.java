package com.smartcommerce.config;

import com.smartcommerce.category.entity.Category;
import com.smartcommerce.category.repository.CategoryRepository;
import com.smartcommerce.common.SlugUtils;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Siembra un catálogo de demostración (categorías y productos) al arrancar.
 * <p>
 * Es <b>idempotente</b>: en cada arranque comprueba si cada categoría/producto
 * ya existe y solo crea lo que falta. Así una base de datos nueva (o levantada
 * desde cero con Docker) queda con el catálogo listo, sin duplicar nada en los
 * arranques siguientes ni tocar los datos existentes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // --- Categorías (3 existentes + 4 nuevas) ---
        Category electronica = seedCategory("Electronica");
        Category hogar = seedCategory("Hogar");
        Category deportes = seedCategory("Deportes");
        Category moda = seedCategory("Moda");
        Category juguetes = seedCategory("Juguetes");
        Category libros = seedCategory("Libros");
        Category belleza = seedCategory("Belleza");

        // --- Productos existentes (para que un clon nuevo también los tenga) ---
        seedProduct("Auriculares Bluetooth Pro", "Auriculares inalámbricos con cancelación de ruido.",
                new BigDecimal("59.99"), 40, "SoundMax", electronica);
        seedProduct("Smartwatch Fit 2", "Reloj inteligente con seguimiento de actividad y sueño.",
                new BigDecimal("129.90"), 25, "Zentech", electronica);
        seedProduct("Altavoz Portatil X", "Altavoz Bluetooth resistente al agua con 12h de batería.",
                new BigDecimal("39.95"), 60, "SoundMax", electronica);
        seedProduct("Teclado Mecanico RGB", "Teclado mecánico retroiluminado para gaming.",
                new BigDecimal("79.00"), 30, "Zentech", electronica);
        seedProduct("Cafetera Espresso", "Cafetera espresso de 15 bares para casa.",
                new BigDecimal("89.00"), 20, "HomeBrew", hogar);
        seedProduct("Lampara LED de Escritorio", "Lámpara LED regulable con puerto USB.",
                new BigDecimal("24.50"), 75, "LumiCo", hogar);
        seedProduct("Esterilla de Yoga", "Esterilla antideslizante de 6mm para yoga y pilates.",
                new BigDecimal("19.99"), 100, "FlexFit", deportes);
        seedProduct("Mochila Trail 30L", "Mochila de senderismo ligera de 30 litros.",
                new BigDecimal("45.00"), 50, "TrailGear", deportes);

        // --- Productos nuevos (10) ---
        seedProduct("Camiseta Basica Algodon", "Camiseta 100% algodón de manga corta.",
                new BigDecimal("12.99"), 120, "BasicWear", moda);
        seedProduct("Sudadera con Capucha", "Sudadera unisex con capucha y bolsillo canguro.",
                new BigDecimal("34.90"), 60, "UrbanFit", moda);
        seedProduct("Zapatillas Running Air", "Zapatillas ligeras con amortiguación para correr.",
                new BigDecimal("79.95"), 35, "Sprintix", moda);
        seedProduct("Peluche Oso Gigante", "Oso de peluche suave de 80cm.",
                new BigDecimal("24.99"), 45, "CuddleCo", juguetes);
        seedProduct("Set de Bloques 500 pzs", "Set de bloques de construcción de 500 piezas.",
                new BigDecimal("39.99"), 30, "BrickLab", juguetes);
        seedProduct("El Arte de Programar", "Guía práctica para escribir código limpio y mantenible.",
                new BigDecimal("29.50"), 80, "TechBooks", libros);
        seedProduct("Novela El Ultimo Faro", "Novela de misterio ambientada en la costa gallega.",
                new BigDecimal("18.90"), 120, "Ediciones Mar", libros);
        seedProduct("Crema Hidratante Facial", "Crema hidratante con ácido hialurónico, 50ml.",
                new BigDecimal("15.99"), 90, "PureSkin", belleza);
        seedProduct("Set de Brochas Maquillaje", "Set de 12 brochas profesionales de maquillaje.",
                new BigDecimal("22.50"), 55, "GlowPro", belleza);
        seedProduct("Cargador USB-C 65W", "Cargador rápido GaN de 65W con dos puertos.",
                new BigDecimal("27.99"), 70, "Zentech", electronica);
    }

    private Category seedCategory(String name) {
        String slug = SlugUtils.toSlug(name);
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .name(name)
                            .slug(slug)
                            .active(true)
                            .build();
                    Category saved = categoryRepository.save(category);
                    log.info("Seed: categoría creada '{}'", name);
                    return saved;
                });
    }

    private void seedProduct(String name, String description, BigDecimal price,
                             int stock, String brand, Category category) {
        if (productRepository.existsByName(name)) {
            return;
        }
        ProductStatus status = stock > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK;
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .brand(brand)
                .status(status)
                .category(category)
                .build();
        productRepository.save(product);
        log.info("Seed: producto creado '{}'", name);
    }
}
