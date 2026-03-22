package com.freezedance.api.config;

import com.freezedance.api.model.*;
import com.freezedance.api.model.enums.ProductType;
import com.freezedance.api.model.enums.Role;
import com.freezedance.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) return;

        // Create categories
        Category cubes = categoryRepository.save(Category.builder()
                .name("Freeze Dried Cubes")
                .slug("cubes")
                .description("100% natural freeze-dried fruit cubes. Crunchy, nutritious, and delicious.")
                .build());

        Category powder = categoryRepository.save(Category.builder()
                .name("Fruit Powder")
                .slug("powder")
                .description("Pure fruit and superfood powders. Perfect for smoothies, baking, and cooking.")
                .build());

        // Freeze Dried Cubes
        createProductWithVariants("Mango Cubes", "mango-cubes",
                "Premium freeze-dried mango cubes made from Alphonso mangoes. Retains natural sweetness and nutrients.",
                "Crunchy Alphonso mango cubes", cubes, ProductType.CUBE,
                new BigDecimal("199"), true);

        createProductWithVariants("Pineapple Cubes", "pineapple-cubes",
                "Tangy and sweet freeze-dried pineapple cubes. Rich in Vitamin C and bromelain.",
                "Tangy tropical pineapple cubes", cubes, ProductType.CUBE,
                new BigDecimal("219"), true);

        createProductWithVariants("Jamun Cubes", "jamun-cubes",
                "Rare freeze-dried jamun (Indian blackberry) cubes. Known for blood sugar management benefits.",
                "Nutritious Indian blackberry cubes", cubes, ProductType.CUBE,
                new BigDecimal("249"), false);

        createProductWithVariants("Banana Cubes", "banana-cubes",
                "Creamy freeze-dried banana cubes packed with potassium and natural energy.",
                "Energy-packed banana cubes", cubes, ProductType.CUBE,
                new BigDecimal("179"), false);

        createProductWithVariants("Jackfruit Cubes", "jackfruit-cubes",
                "Exotic freeze-dried jackfruit cubes. The jack of all fruits with a bunch of health benefits.",
                "Exotic jackfruit cubes", cubes, ProductType.CUBE,
                new BigDecimal("233"), true);

        // Powder Products
        createProductWithVariants("Jamun Powder", "jamun-powder",
                "Pure jamun fruit powder. Excellent for managing blood sugar levels and boosting immunity.",
                "Pure jamun superfood powder", powder, ProductType.POWDER,
                new BigDecimal("299"), false);

        createProductWithVariants("Mango Powder", "mango-powder",
                "100% natural mango powder made from Alphonso mangoes. Perfect for smoothies, desserts, and shakes.",
                "Alphonso mango smoothie powder", powder, ProductType.POWDER,
                new BigDecimal("279"), true);

        createProductWithVariants("Pineapple Powder", "pineapple-powder",
                "Pure pineapple powder rich in enzymes and Vitamin C. Great for cooking and beverages.",
                "Enzyme-rich pineapple powder", powder, ProductType.POWDER,
                new BigDecimal("269"), false);

        createProductWithVariants("Moringa Powder", "moringa-powder",
                "Organic moringa leaf powder — the ultimate superfood. Packed with vitamins, minerals, and antioxidants.",
                "Organic moringa superfood powder", powder, ProductType.POWDER,
                new BigDecimal("349"), true);

        // Create admin user
        if (!userRepository.existsByEmail("admin@freezedance.in")) {
            userRepository.save(User.builder()
                    .email("admin@freezedance.in")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .fullName("FreezeDance Admin")
                    .role(Role.ADMIN)
                    .build());
        }
    }

    private void createProductWithVariants(String name, String slug, String description,
                                            String shortDesc, Category category,
                                            ProductType type, BigDecimal basePrice,
                                            boolean featured) {
        Product product = Product.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .shortDesc(shortDesc)
                .category(category)
                .productType(type)
                .basePrice(basePrice)
                .isFeatured(featured)
                .metaTitle(name + " | FreezeDance - Premium Freeze Dried Fruits")
                .metaDesc(shortDesc + ". 100% Natural, No Preservatives. Shop now at FreezeDance.")
                .build();

        // Weight variants: 50g, 100g, 250g, 500g
        BigDecimal[] multipliers = {
            new BigDecimal("0.35"),  // 50g
            new BigDecimal("1.0"),   // 100g (base)
            new BigDecimal("2.3"),   // 250g (slight discount)
            new BigDecimal("4.2")    // 500g (more discount)
        };
        int[] weights = {50, 100, 250, 500};

        for (int i = 0; i < weights.length; i++) {
            String sku = "FD-" + slug.toUpperCase().replace("-", "") + "-" + weights[i] + "G";
            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .weightGrams(weights[i])
                    .price(basePrice.multiply(multipliers[i]).setScale(2, java.math.RoundingMode.HALF_UP))
                    .stockQty(100)
                    .sku(sku)
                    .build();
            product.getVariants().add(variant);
        }

        productRepository.save(product);
    }
}
