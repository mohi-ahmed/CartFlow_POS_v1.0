package com.sparktech.cart_pos_v1_0.Specifications;


import com.sparktech.cart_pos_v1_0.Product;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ProductSpecifications {

    public static Specification<Product> isInactive() {
        return (root, query, cb) -> cb.isFalse(root.get("active"));
    }

    public static Specification<Product> hasKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null; // no filter
        }
        String likePattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), likePattern),
                cb.like(cb.lower(root.get("category")), likePattern)
        );
    }

    public static Specification<Product> deactivatedAfter(LocalDateTime from) {
        if (from == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("deactivatedAt"), from);
    }

    public static Specification<Product> deactivatedBefore(LocalDateTime to) {
        if (to == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("deactivatedAt"), to);
    }
}
