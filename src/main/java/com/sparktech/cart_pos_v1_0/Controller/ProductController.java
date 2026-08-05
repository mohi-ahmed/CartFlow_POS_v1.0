package com.sparktech.cart_pos_v1_0.Controller;


import com.sparktech.cart_pos_v1_0.DTO.RemovedProductStatsDto;
import com.sparktech.cart_pos_v1_0.Repositories.ProductRepository;
import com.sparktech.cart_pos_v1_0.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor // 1... er kaj kore
@RequestMapping("/POS")
public class ProductController {


    private final ProductInterface productInterface;

//  1..  public ProductController(ProductInterface productInterface) {
//        this.productInterface = productInterface;
//    }

    @GetMapping("/add")
    public String showAddProduct(Model model) {
        model.addAttribute("product", new Product());
        return "add-product";
    }

    @PostMapping("/submit-product")
    public String submitAddProduct(@Valid @ModelAttribute Product product, BindingResult bindingResult) {

        if(bindingResult.hasErrors()){
            return "add-product";
        }

        productInterface.save(product);
        log.info("products are added: {}", product);

        return "redirect:/POS/add"; // view te issue ache /POS/add aita astache na
    }

    //Products List Table [products.html view]
    @GetMapping("/productList")
    public String productsList(@RequestParam(required = false) String keyword, Model model) {

        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("p", productInterface.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(keyword, keyword));
            model.addAttribute("keyword", keyword); // keeps the search box filled after searching
        } else {
            model.addAttribute("p", productInterface.findAll());
        }

        model.addAttribute("totalCount", productInterface.count());
        model.addAttribute("inStockCount", productInterface.countByStockGreaterThan(10));
        model.addAttribute("lowStockCount", productInterface.countByStockLessThanEqualAndStockGreaterThan(10, 0));
        model.addAttribute("outOfStockCount", productInterface.countByStock(0));

        return "products";
    }

    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productInterface.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        model.addAttribute("product", product);
        return "edit-product";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id, @Valid @ModelAttribute Product product, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "edit-product";
        }

        product.setId(id); // make sure the update targets the right row, not a new insert
        productInterface.save(product);

        log.info("Updated product : {}", product);
        return "redirect:/POS/productList";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        if (!productInterface.existsById(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        productInterface.deleteById(id);
        log.info("Deleted product with id: {}", id);
        return "redirect:/POS/productList";
    }
}
