package com.sparktech.cart_pos_v1_0.Services;

import com.sparktech.cart_pos_v1_0.DTO.ExpenseStats;
import com.sparktech.cart_pos_v1_0.Expense;
import com.sparktech.cart_pos_v1_0.Repositories.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public void saveExpense(Expense expense) {
       expenseRepository.save(expense);
    }

    public Expense getById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    public List<Expense> searchExpense(String keyword, LocalDate dateFrom, LocalDate dateTo){
        List<Expense> expenseSearch;
        if(keyword != null && keyword.isBlank()){
            expenseSearch = expenseRepository.
                    findAllByActiveTrueAndTitleContainingIgnoreCaseOrActiveTrueAndCategoryContainingIgnoreCase(keyword,keyword);
        }else{
            expenseSearch = expenseRepository.findAllByActiveTrueOrderByExpenseDateDesc();
        }
        return expenseSearch.stream().
                filter(e -> dateFrom == null || !e.getExpenseDate().isBefore(dateFrom)).
                filter(e -> dateTo == null || !e.getExpenseDate().isAfter(dateTo)).toList();
    }

    // ---------- Search: removed (inactive) expenses, filtered by deletedAt date range ----------
    public List<Expense> searchRemovedExpenses(String keyword, LocalDate dateFrom, LocalDate dateTo) {
        List<Expense> base;

        if (keyword != null && !keyword.isBlank()) {
            base = expenseRepository
                    .findByActiveFalseAndTitleContainingIgnoreCaseOrActiveFalseAndCategoryContainingIgnoreCase(
                            keyword, keyword);
        } else {
            base = expenseRepository.findAllByActiveFalseOrderByExpenseDateDesc();
        }

        return base.stream()
                .filter(e -> dateFrom == null || e.getDeletedAt() == null
                        || !e.getDeletedAt().toLocalDate().isBefore(dateFrom))
                .filter(e -> dateTo == null || e.getDeletedAt() == null
                        || !e.getDeletedAt().toLocalDate().isAfter(dateTo))
                .toList();
    }

    public ExpenseStats calculateStats(List<Expense> expenses){
        long total = expenses.size();
        double highest = expenses.stream().mapToDouble(Expense :: getAmount).max().orElse(0.0);
        double sum = expenses.stream().mapToDouble(Expense :: getAmount).sum();
        return new ExpenseStats(total, highest, sum);
    }

    public double getTodayTotal() {
        LocalDate today = LocalDate.now();
        return expenseRepository.findAllByActiveTrueOrderByExpenseDateDesc().stream()
                .filter(e -> e.getExpenseDate().isEqual(today))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public void deactivateExpense(Long id) {
        Expense expense = getById(id);
        expense.setActive(false);
        expense.setDeletedAt(LocalDateTime.now());
        expenseRepository.save(expense);
    }

    public void restoreExpense(Long id) {
        Expense expense = getById(id);
        expense.setActive(true);
        expense.setDeletedAt(null);
        expenseRepository.save(expense);
    }

    public List<Expense> getRemovedExpenses() {
        return expenseRepository.findAllByActiveFalseOrderByExpenseDateDesc();
    }
}
