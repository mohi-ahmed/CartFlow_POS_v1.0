package com.sparktech.cart_pos_v1_0.Controller;

import com.sparktech.cart_pos_v1_0.Product;
import com.sparktech.cart_pos_v1_0.Sale;
import com.sparktech.cart_pos_v1_0.Interface.ProductInterface;
import com.sparktech.cart_pos_v1_0.Interface.SaleInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sales")
public class SaleController {

    private final ProductInterface productInterface;

    private final SaleInterface saleInterface;

    @GetMapping
    public String salesPage(Model model){

        model.addAttribute("sale",new Sale());

        model.addAttribute("products",productInterface.findAll());

        model.addAttribute("sales",saleInterface.findAll());

        return "sales";
    }

    @PostMapping("/save")
    public String saveSale(@Valid @ModelAttribute Sale sale,
                           BindingResult bindingResult,
                           Model model){

        if(bindingResult.hasErrors()){

            model.addAttribute("products",productInterface.findAll());

            model.addAttribute("sales",saleInterface.findAll());

            return "sales";
        }

        Product product = productInterface.findById(sale.getProduct().getId())
                .orElseThrow();

        if(product.getStock() < sale.getQuantity()){

            bindingResult.rejectValue(
                    "quantity",
                    "error.quantity",
                    "Not enough stock"
            );

            model.addAttribute("products",productInterface.findAll());

            model.addAttribute("sales",saleInterface.findAll());

            return "sales";
        }

        sale.setProduct(product);

        sale.setTotalPrice(product.getPrice()*sale.getQuantity());

        sale.setSaleDate(LocalDate.now());

        product.setStock(product.getStock()-sale.getQuantity());

        productInterface.save(product);

        saleInterface.save(sale);

        return "redirect:/sales";
    }

}