package com.sparktech.cart_pos_v1_0.Repositories;

import com.sparktech.cart_pos_v1_0.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<com.sparktech.cart_pos_v1_0.Customer, Long> {
    Optional<Customer> findByPhone(String phone);
}