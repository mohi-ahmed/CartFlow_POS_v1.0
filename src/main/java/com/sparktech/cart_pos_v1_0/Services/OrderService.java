package com.sparktech.cart_pos_v1_0.Services;

import com.sparktech.cart_pos_v1_0.Customer;
import com.sparktech.cart_pos_v1_0.DTO.CartItemDto;
import com.sparktech.cart_pos_v1_0.Order;

import com.sparktech.cart_pos_v1_0.OrderItem;
import com.sparktech.cart_pos_v1_0.Product;
import com.sparktech.cart_pos_v1_0.Repositories.CustomerRepository;
import com.sparktech.cart_pos_v1_0.Repositories.OrderRepository;
import com.sparktech.cart_pos_v1_0.Repositories.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public Order placeOrder(List<CartItemDto> cartItems, String customerPhone) {

        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart khali — kono item add kora hoyni");
        }

        Order order = new Order();
        order.setOrderDateTime(LocalDateTime.now());

        // ---------- customer optional link (dokandar chaile phone number bosabe) ----------
        if (customerPhone != null && !customerPhone.isBlank()) {
            Customer customer = customerRepository.findByPhone(customerPhone)
                    .orElseGet(() -> {
                        Customer c = new Customer();
                        c.setPhone(customerPhone);
                        return customerRepository.save(c);
                    });
            order.setCustomer(customer);
        }

        List<OrderItem> items = new ArrayList<>();
        double total = 0;

        for (CartItemDto cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + cartItem.getProductId()));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException(
                        product.getName() + " -er stock (" + product.getStock() +
                                ") cart-er quantity (" + cartItem.getQuantity() + ") theke kom");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(product.getPrice());               // dam-er snapshot
            item.setLineTotal(product.getPrice() * cartItem.getQuantity());
            items.add(item);

            total += item.getLineTotal();

            // stock kome jabe
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setItems(items);
        order.setTotalAmount(total);

        return orderRepository.save(order);
    }
}