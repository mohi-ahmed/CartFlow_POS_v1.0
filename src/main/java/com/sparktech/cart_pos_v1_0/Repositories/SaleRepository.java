package com.sparktech.cart_pos_v1_0.Repositories;

import com.sparktech.cart_pos_v1_0.DTO.DailyAmountDto;
import com.sparktech.cart_pos_v1_0.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale,Long> {

    List<Sale> findBySaleDate(LocalDate saleDate);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate = :date")
    long countByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Sale s WHERE s.saleDate = :date")
    long sumQuantityByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.totalPrice), 0) FROM Sale s WHERE s.saleDate = :date")
    double sumRevenueByDate(@Param("date") LocalDate date);

    @Query("SELECT new com.sparktech.cart_pos_v1_0.DTO.DailyAmountDto(s.saleDate, SUM(s.totalPrice)) " +
            "FROM Sale s WHERE s.saleDate BETWEEN :from AND :to GROUP BY s.saleDate ORDER BY s.saleDate")
    List<DailyAmountDto> sumSalesGroupByDate(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // sumQuantityGroupByProduct mushe fela holo — ei kaj ekhon OrderItemRepository korche
}

//package com.sparktech.cart_pos_v1_0.Repositories;
//
//import com.sparktech.cart_pos_v1_0.DTO.DailyAmountDto;
//import com.sparktech.cart_pos_v1_0.DTO.ProductVelocityDto;
//import com.sparktech.cart_pos_v1_0.Sale;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.time.LocalDate;
//import java.util.List;
////@Autowired
//
//public interface SaleRepository extends JpaRepository<Sale,Long> {
//    //List<Sale>findByByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);
//
//    List<Sale> findBySaleDate(LocalDate saleDate);
//
//    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate = :date")
//    long countByDate(@Param("date") LocalDate date);
//
//    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Sale s WHERE s.saleDate = :date")
//    long sumQuantityByDate(@Param("date") LocalDate date);
//
//    @Query("SELECT COALESCE(SUM(s.totalPrice), 0) FROM Sale s WHERE s.saleDate = :date")
//    double sumRevenueByDate(@Param("date") LocalDate date);
//
//    @Query("SELECT new com.sparktech.cart_pos_v1_0.DTO.DailyAmountDto(s.saleDate, SUM(s.totalPrice)) " +
//            "FROM Sale s WHERE s.saleDate BETWEEN :from AND :to GROUP BY s.saleDate ORDER BY s.saleDate")
//    List<DailyAmountDto> sumSalesGroupByDate(@Param("from") LocalDate from, @Param("to") LocalDate to);
//
//    @Query("SELECT new com.sparktech.cart_pos_v1_0.DTO.ProductVelocityDto(s.product.name, SUM(s.quantity)) " +
//            "FROM Sale s WHERE s.saleDate BETWEEN :from AND :to GROUP BY s.product.name ORDER BY SUM(s.quantity) DESC")
//    List<ProductVelocityDto> sumQuantityGroupByProduct(@Param("from") LocalDate from, @Param("to") LocalDate to);
//
//  //  List<Sale>
//}
