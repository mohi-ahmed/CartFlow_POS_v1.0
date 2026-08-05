package com.sparktech.cart_pos_v1_0.Repositories;

import com.sparktech.cart_pos_v1_0.DTO.CategoryAmountDto;
import com.sparktech.cart_pos_v1_0.DTO.DailyAmountDto;
import com.sparktech.cart_pos_v1_0.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByActiveTrueOrderByExpenseDateDesc();
    List<Expense> findAllByActiveFalseOrderByExpenseDateDesc();
    List<Expense> findAllByActiveTrueAndTitleContainingIgnoreCaseOrActiveTrueAndCategoryContainingIgnoreCase
            (String title, String category);
    // ---------- Keyword search (title OR category) among removed (inactive) expenses ----------
    List<Expense> findByActiveFalseAndTitleContainingIgnoreCaseOrActiveFalseAndCategoryContainingIgnoreCase(
            String title, String category);

    @Query("SELECT SUM(e.amount) FROM  Expense e WHERE e.active = true")
    Double findTotalActiveAmount();

    @Query("select max (e.amount) from Expense e where e.active = true ")
    Double findHighestActiveAmount();

    @Query("SELECT new com.sparktech.cart_pos_v1_0.DTO.DailyAmountDto(e.expenseDate, SUM(e.amount)) " +
            "FROM Expense e WHERE e.active = true AND e.expenseDate BETWEEN :from AND :to GROUP BY e.expenseDate ORDER BY e.expenseDate")
    List<DailyAmountDto> sumExpenseGroupByDate(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT new com.sparktech.cart_pos_v1_0.DTO.CategoryAmountDto(e.category, SUM(e.amount)) " +
            "FROM Expense e WHERE e.active = true AND e.expenseDate BETWEEN :from AND :to GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<CategoryAmountDto> sumExpenseGroupByCategory(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
