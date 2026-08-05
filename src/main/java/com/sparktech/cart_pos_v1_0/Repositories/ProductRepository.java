package com.sparktech.cart_pos_v1_0.Repositories;

import com.sparktech.cart_pos_v1_0.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);
    long countByStockGreaterThan(int stock);
    long countByStockLessThanEqualAndStockGreaterThan(int stockMax,int stockMin);
    long countByStock(int stock);


    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);


    Optional<Product> findByNameIgnoreCaseAndCategoryIgnoreCase(String name, String category);

    @Query("SELECT DISTINCT p.name FROM Product p")
    List<String> findAllDistinctNames();

    List<Product> findByActiveTrue();
    List<Product> findByActiveFalse();


}