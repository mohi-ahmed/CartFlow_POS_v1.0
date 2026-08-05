package com.sparktech.cart_pos_v1_0.Controller;


import com.sparktech.cart_pos_v1_0.DTO.RemovedProductStatsDto;
import com.sparktech.cart_pos_v1_0.Repositories.ProductRepository;
import com.sparktech.cart_pos_v1_0.Product;
import com.sparktech.cart_pos_v1_0.Services.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor // 1... er kaj kore
@RequestMapping("/POS")
public class ProductController {
    private final ProductService productService;


//    private final ProductRepository productRepository;

//  1..  public ProductController(ProductInterface productInterface) {
//        this.productInterface = productInterface;
//    }

    @GetMapping("/add")
    public String showAddProduct( Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("allProductsNames",  productService.getSuggestedName());
        return "add-product";
    }

    @PostMapping("/submit-product")
    public String submitAddProduct(@Valid @ModelAttribute Product product, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return "add-product";
        }
        productService.addOrRestockProduct(product);
        log.info("products are added: {}", product);
        return "redirect:/POS/add"; // view te issue ache /POS/add aita astache na
    }

    //Products List Table [products.html view]
    @GetMapping("/productList")
    public String productsList(@RequestParam(required = false) String keyword, Model model) {
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("p", productService.search(keyword, keyword));
            model.addAttribute("keyword", keyword); // keeps the search box filled after searching
        } else {
            model.addAttribute("p", productService.getAll());
        }
        model.addAttribute("totalCount", productService.productCount());
        model.addAttribute("inStockCount", productService.inStockCount());
        model.addAttribute("lowStockCount", productService.lowStockCount());
        model.addAttribute("outOfStockCount", productService.outOfStockCount());

        return "products";
    }

    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);
        return "edit-product";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id, @Valid @ModelAttribute Product product,
                                BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "edit-product";
        }
        try {
            productService.updateProduct(id, product);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/POS/productList";
    }

//    @PostMapping("/delete/{id}")
//    public String deleteProduct(@PathVariable Long id) {
//        productService.deleteById(id);
//        log.info("Deleted product with id: {}", id);
//        return "redirect:/POS/productList";
//    }
@PostMapping("/delete/{id}")
public String deactivateProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        productService.deactivateProduct(id);
        redirectAttributes.addFlashAttribute("success", "Product removed from active catalogue.");
    } catch (EntityNotFoundException e) {
        redirectAttributes.addFlashAttribute("error", "Product not found.");
    }
    return "redirect:/POS/productList";
}

    @GetMapping("/removed-products")
    public String showRemovedProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {

        List<Product> removed = productService.searchRemovedProducts(keyword, dateFrom, dateTo);
        RemovedProductStatsDto stats = productService.calculateStats(removed);

        model.addAttribute("removedProducts", removed);
        model.addAttribute("keyword", keyword);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("stats", stats);

        return "removed";
    }
    // ---------- Restore ----------
    @GetMapping("/restore/{id}")
    public String restoreProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.restoreProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product restored successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Product not found.");
        }
        return "redirect:/POS/removed-products";
    }
}
