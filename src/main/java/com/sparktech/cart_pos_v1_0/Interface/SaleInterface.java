package com.sparktech.cart_pos_v1_0.Interface;

import com.sparktech.cart_pos_v1_0.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleInterface extends JpaRepository<Sale,Long> {
    //List<Sale>findByByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);

}
