package com.sparktech.cart_pos_v1_0.Interface;

import com.sparktech.cart_pos_v1_0.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductInterface extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);
    long countByStockGreaterThan(int stock);
    long countByStockLessThanEqualAndStockGreaterThan(int stockMax,int stockMin);
    long countByStock(int stock);

}