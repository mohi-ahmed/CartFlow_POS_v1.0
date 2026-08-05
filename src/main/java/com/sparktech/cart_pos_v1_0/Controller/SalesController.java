package com.sparktech.cart_pos_v1_0.Controller;

import com.sparktech.cart_pos_v1_0.DTO.OrderRequestDto;
import com.sparktech.cart_pos_v1_0.Order;
import com.sparktech.cart_pos_v1_0.Repositories.OrderItemRepository;
import com.sparktech.cart_pos_v1_0.Repositories.OrderRepository;
import com.sparktech.cart_pos_v1_0.Repositories.ProductRepository;
import com.sparktech.cart_pos_v1_0.Services.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class SalesController {

    private final ProductRepository productInterface;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;

    @GetMapping("/sale")
    public String sealPage(Model model) {
        model.addAttribute("products", productInterface.findByActiveTrue());

        LocalDate today = LocalDate.now();
        model.addAttribute("todayOrders", orderRepository.findByOrderDate(today));
        model.addAttribute("transactionCount", orderRepository.countByOrderDate(today));
        model.addAttribute("itemsSold", orderItemRepository.sumQuantityByDate(today));
        model.addAttribute("revenue", orderRepository.sumRevenueByDate(today));

        return "sale";
    }

    // ---------- traditional form POST — hidden inputs diye cart pathano hoy ----------
    @PostMapping("/order")
    public String placeOrder(@ModelAttribute OrderRequestDto request, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.placeOrder(request.getItems(), request.getCustomerPhone());
            redirectAttributes.addFlashAttribute("success",
                    "Order #" + order.getId() + " shofol — Total: ৳" + order.getTotalAmount());
        } catch (EntityNotFoundException | IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Kichu ekta bhul hoyeche, abar try koro");
        }
        return "redirect:/sale";
    }
}



//package com.sparktech.cart_pos_v1_0.Controller;
//
//import com.sparktech.cart_pos_v1_0.Repositories.ProductRepository;
//import com.sparktech.cart_pos_v1_0.Repositories.SaleRepository;
//import com.sparktech.cart_pos_v1_0.Product;
//import com.sparktech.cart_pos_v1_0.Sale;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//
//import java.time.LocalDate;
//
//@Controller
//@RequiredArgsConstructor
//public class SalesController {
//
//    private final ProductRepository productInterface;
//    private final SaleRepository saleRepository;
//    @GetMapping("/sale")
//    public String sealPage(Model model) {
//        model.addAttribute("products", productInterface.findAll());
//        model.addAttribute("sale", new Sale());
//
//        Sale sale = new Sale();
//        sale.setSaleDate(LocalDate.now()); // default date set in Java, not JS
//        model.addAttribute("sale", sale);
//
//        LocalDate today = LocalDate.now();
//        model.addAttribute("todaySalesTable", saleRepository.findBySaleDate(today));
//        model.addAttribute("transactionCount", saleRepository.countByDate(today));
//        model.addAttribute("itemsSold", saleRepository.sumQuantityByDate(today));
//        model.addAttribute("revenue", saleRepository.sumRevenueByDate(today));
//
//        return "sale";
//    }
//
//    @PostMapping("/sale")
//    public String saleSave(@Valid @ModelAttribute Sale sale, BindingResult bindingResult, Model model) {
//
//        if(bindingResult.hasErrors()){
//            return "sale";
//        }
//
//        Product product = productInterface.findById(sale.getProduct().getId()).
//                orElseThrow(() -> new RuntimeException("Product are not found"));
//
//        sale.setProduct(product);
//        sale.setTotalPrice(product.getPrice() * sale.getQuantity());
//
//        product.setStock(product.getStock() - sale.getQuantity());
//        productInterface.save(product);
//
//        saleRepository.save(sale);
//
//        return "redirect:/sale";
//
//    }
//}
