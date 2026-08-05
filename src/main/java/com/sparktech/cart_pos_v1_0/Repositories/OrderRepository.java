package com.sparktech.cart_pos_v1_0.Repositories;

import com.sparktech.cart_pos_v1_0.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.orderDateTime BETWEEN :fromDateTime AND :toDateTime ORDER BY o.orderDateTime DESC")
    List<Order> findByDateRange(@Param("fromDateTime") LocalDateTime fromDateTime, @Param("toDateTime") LocalDateTime toDateTime);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDateTime BETWEEN :fromDateTime AND :toDateTime")
    long countByDateRange(@Param("fromDateTime") LocalDateTime fromDateTime, @Param("toDateTime") LocalDateTime toDateTime);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDateTime BETWEEN :fromDateTime AND :toDateTime")
    double sumRevenueByDateRange(@Param("fromDateTime") LocalDateTime fromDateTime, @Param("toDateTime") LocalDateTime toDateTime);

    default List<Order> findByOrderDate(LocalDate date) {
        return findByDateRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay().minusNanos(1));
    }

    default long countByOrderDate(LocalDate date) {
        return countByDateRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay().minusNanos(1));
    }

    default double sumRevenueByDate(LocalDate date) {
        return sumRevenueByDateRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay().minusNanos(1));
    }
}

//package com.sparktech.cart_pos_v1_0.Repositories;
//
//import com.sparktech.cart_pos_v1_0.Order;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public interface OrderRepository extends JpaRepository<Order, Long> {
//
//    // ajker shob order dekhar jonno (orderDateTime theke date ber kore compare kora)
//    @Query("SELECT o FROM Order o WHERE FUNCTION('DATE', o.orderDateTime) = :date ORDER BY o.orderDateTime DESC")
//    List<Order> findByOrderDate(@Param("date") LocalDate date);
//
//    @Query("SELECT COUNT(o) FROM Order o WHERE FUNCTION('DATE', o.orderDateTime) = :date")
//    long countByOrderDate(@Param("date") LocalDate date);
//
//    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE FUNCTION('DATE', o.orderDateTime) = :date")
//    double sumRevenueByDate(@Param("date") LocalDate date);
//}