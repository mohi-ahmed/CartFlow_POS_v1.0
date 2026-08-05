package com.sparktech.cart_pos_v1_0.Repositories;

import com.sparktech.cart_pos_v1_0.DTO.DailyAmountDto;
import com.sparktech.cart_pos_v1_0.DTO.ProductVelocityDto;
import com.sparktech.cart_pos_v1_0.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // ---------- raw projection — kono FUNCTION() nai, tai type-mismatch hobe na ----------
    interface DateAmountProjection {
        LocalDateTime getOrderDateTime();
        Double getLineTotal();
    }

    @Query("SELECT oi.order.orderDateTime AS orderDateTime, oi.lineTotal AS lineTotal " +
            "FROM OrderItem oi WHERE oi.order.orderDateTime BETWEEN :fromDateTime AND :toDateTime")
    List<DateAmountProjection> findAmountsBetween(@Param("fromDateTime") LocalDateTime fromDateTime,
                                                  @Param("toDateTime") LocalDateTime toDateTime);

    // ---------- default method — Java-e date diye group kore DailyAmountDto banay ----------
    default List<DailyAmountDto> sumSalesGroupByDate(LocalDate from, LocalDate to) {
        List<DateAmountProjection> rows = findAmountsBetween(from.atStartOfDay(), endOfDay(to));

        Map<LocalDate, Double> grouped = new TreeMap<>();
        for (DateAmountProjection row : rows) {
            grouped.merge(row.getOrderDateTime().toLocalDate(), row.getLineTotal(), Double::sum);
        }

        List<DailyAmountDto> result = new ArrayList<>();
        grouped.forEach((date, amount) -> result.add(new DailyAmountDto(date, amount)));
        return result;
    }

    // ---------- product-wise quantity (FUNCTION lagchilo na eikhane, tobuo range-based e bodlano holo consistency-r jonno) ----------
    @Query("SELECT new com.sparktech.cart_pos_v1_0.DTO.ProductVelocityDto(oi.product.id, oi.product.name, SUM(oi.quantity)) " +
            "FROM OrderItem oi WHERE oi.order.orderDateTime BETWEEN :fromDateTime AND :toDateTime " +
            "GROUP BY oi.product.id, oi.product.name")
    List<ProductVelocityDto> sumQuantityByProductInRangeRaw(@Param("fromDateTime") LocalDateTime fromDateTime,
                                                            @Param("toDateTime") LocalDateTime toDateTime);

    default List<ProductVelocityDto> sumQuantityByProductInRange(LocalDate from, LocalDate to) {
        return sumQuantityByProductInRangeRaw(from.atStartOfDay(), endOfDay(to));
    }

    @Query("SELECT new com.sparktech.cart_pos_v1_0.DTO.ProductVelocityDto(oi.product.id, oi.product.name, SUM(oi.quantity)) " +
            "FROM OrderItem oi WHERE oi.order.orderDateTime BETWEEN :fromDateTime AND :toDateTime " +
            "GROUP BY oi.product.id, oi.product.name ORDER BY SUM(oi.quantity) DESC")
    List<ProductVelocityDto> sumQuantityGroupByProductRaw(@Param("fromDateTime") LocalDateTime fromDateTime,
                                                          @Param("toDateTime") LocalDateTime toDateTime);

    default List<ProductVelocityDto> sumQuantityGroupByProduct(LocalDate from, LocalDate to) {
        return sumQuantityGroupByProductRaw(from.atStartOfDay(), endOfDay(to));
    }

    // ---------- earliest order date ----------
    @Query("SELECT MIN(oi.order.orderDateTime) FROM OrderItem oi")
    LocalDateTime findEarliestOrderDateTime();

    default LocalDate findEarliestOrderDate() {
        LocalDateTime dt = findEarliestOrderDateTime();
        return dt == null ? null : dt.toLocalDate();
    }

    // ---------- ajker item-sold count ----------
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.order.orderDateTime BETWEEN :fromDateTime AND :toDateTime")
    long sumQuantityByDateRange(@Param("fromDateTime") LocalDateTime fromDateTime, @Param("toDateTime") LocalDateTime toDateTime);

    default long sumQuantityByDate(LocalDate date) {
        return sumQuantityByDateRange(date.atStartOfDay(), endOfDay(date));
    }

    // ---------- helper — ekta din-er shesh muhurto ----------
    private LocalDateTime endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusNanos(1);
    }
}


//package com.sparktech.cart_pos_v1_0.Repositories;
//
//import com.sparktech.cart_pos_v1_0.DTO.DailyAmountDto;
//import com.sparktech.cart_pos_v1_0.DTO.ProductVelocityDto;
//import com.sparktech.cart_pos_v1_0.OrderItem;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
//
//    // ---------- Report/AI feature gulor jonno (ager SaleRepository er moto, kintu OrderItem theke) ----------
//
//    @Query("SELECT new com.sparktech.cart_pos_v1_0.DTO.DailyAmountDto(FUNCTION('DATE', oi.order.orderDateTime), SUM(oi.lineTotal)) " +
//            "FROM OrderItem oi WHERE FUNCTION('DATE', oi.order.orderDateTime) BETWEEN :from AND :to " +
//            "GROUP BY FUNCTION('DATE', oi.order.orderDateTime) ORDER BY FUNCTION('DATE', oi.order.orderDateTime)")
//    List<DailyAmountDto> sumSalesGroupByDate(@Param("from") LocalDate from, @Param("to") LocalDate to);
//
//    @Query("SELECT new com.sparktech.cart_pos_v1_0.DTO.ProductVelocityDto(oi.product.id, oi.product.name, SUM(oi.quantity)) " +
//            "FROM OrderItem oi WHERE FUNCTION('DATE', oi.order.orderDateTime) BETWEEN :from AND :to " +
//            "GROUP BY oi.product.id, oi.product.name")
//    List<ProductVelocityDto> sumQuantityByProductInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);
//
//    @Query("SELECT MIN(FUNCTION('DATE', oi.order.orderDateTime)) FROM OrderItem oi")
//    LocalDate findEarliestOrderDate();
//
//    @Query("SELECT new com.sparktech.cart_pos_v1_0.DTO.ProductVelocityDto(oi.product.id, oi.product.name, SUM(oi.quantity)) " +
//            "FROM OrderItem oi WHERE FUNCTION('DATE', oi.order.orderDateTime) BETWEEN :from AND :to " +
//            "GROUP BY oi.product.id, oi.product.name ORDER BY SUM(oi.quantity) DESC")
//    List<ProductVelocityDto> sumQuantityGroupByProduct(@Param("from") LocalDate from, @Param("to") LocalDate to);
//
//    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE FUNCTION('DATE', oi.order.orderDateTime) = :date")
//    long sumQuantityByDate(@Param("date") LocalDate date);
//}