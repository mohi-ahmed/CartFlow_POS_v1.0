package com.sparktech.cart_pos_v1_0.Controller;

import com.sparktech.cart_pos_v1_0.DTO.ExpenseStats;
import com.sparktech.cart_pos_v1_0.Expense;
import com.sparktech.cart_pos_v1_0.Services.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // ---------- List + Add form (default view) ----------
    @GetMapping("/expenses")
    public String showExpenses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {

        List<Expense> expenses = expenseService.searchExpense(keyword, dateFrom, dateTo);
        ExpenseStats stats = expenseService.calculateStats(expenses);

        model.addAttribute("expense", new Expense());
        model.addAttribute("expenses", expenses);
        model.addAttribute("stats", stats);
        model.addAttribute("keyword", keyword);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);

        return "expenses";
    }

    // ---------- Save (Add new expense only) ----------
    @PostMapping("/save")
    public String saveExpense(@Valid @ModelAttribute("expense") Expense expense,
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            List<Expense> expenses = expenseService.searchExpense(null, null, null);
            model.addAttribute("expenses", expenses);
            model.addAttribute("stats", expenseService.calculateStats(expenses));
            // "expense" is already in the model via @ModelAttribute, keeps entered values on error
            return "expenses";
        }
        expenseService.saveExpense(expense);
        return "redirect:/expenses";
    }

    // ---------- Edit form (GET — loads existing data into its own page) ----------
    @GetMapping("/edit/{id}")
    public String editExpense(@PathVariable Long id, Model model) {
        model.addAttribute("expense", expenseService.getById(id));
        return "edit-expense";
    }

    // ---------- Update (POST — saves the edited expense) ----------
    @PostMapping("/update/{id}")
    public String updateExpense(@PathVariable Long id,
                                @Valid @ModelAttribute("expense") Expense expense,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            // stay on the edit page itself so validation errors show next to the right fields
            return "edit-expense";
        }
        expense.setId(id);
        expenseService.saveExpense(expense);
        return "redirect:/expenses";
    }

    // ---------- Soft delete ----------
    @PostMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            expenseService.deactivateExpense(id);
            redirectAttributes.addFlashAttribute("success", "Expense removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Expense not found.");
        }
        return "redirect:/expenses";
    }

    // ---------- Removed Expenses page (with search + date filter) ----------
    @GetMapping("/removed")
    public String showRemovedExpenses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {

        List<Expense> removedExpenses = expenseService.searchRemovedExpenses(keyword, dateFrom, dateTo);

        model.addAttribute("removedExpenses", removedExpenses);
        model.addAttribute("keyword", keyword);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);

        return "removed-expense";
    }

    // ---------- Restore ----------
    @PostMapping("/restore/{id}")
    public String restoreExpense(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            expenseService.restoreExpense(id);
            redirectAttributes.addFlashAttribute("success", "Expense restored successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Expense not found.");
        }
        return "redirect:/removed";
    }
}