package com.sparktech.cart_pos_v1_0.Services;

import com.sparktech.cart_pos_v1_0.DTO.RemovedProductStatsDto;
import com.sparktech.cart_pos_v1_0.Product;
import com.sparktech.cart_pos_v1_0.Repositories.ProductRepository;
import com.sparktech.cart_pos_v1_0.Specifications.ProductSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public void addOrRestockProduct(Product product) {
        Optional<Product> existing = productRepository.
                findByNameIgnoreCaseAndCategoryIgnoreCase(product.getName(), product.getCategory());
        if(existing.isPresent()){
            Product existingProduct = existing.get();
            existingProduct.setPrice(product.getPrice()); // latest price
            existingProduct.setStock(existingProduct.getStock() + product.getStock());
            productRepository.save(existingProduct);
        }else{
            productRepository.save(product);
        }
    }
    public void updateProduct(Long id, Product product) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        // ---------- notun name+category deye onno kono ALADA product-er sathe collision hocche kina check ----------
        List<Product> matches = productRepository
                .findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(product.getName(), product.getCategory());

        boolean collidesWithAnotherProduct = matches.stream()
                .anyMatch(p -> !p.getId().equals(id));

        if (collidesWithAnotherProduct) {
            throw new IllegalStateException(
                    "'" + product.getName() + "' (" + product.getCategory() + ") — ei name o category-r product already ache. "
                            + "Eivabe edit korle duplicate hoye jabe, tai ei change ta save kora jayni. "
                            + "Bhul kore alada category-te add hoye gele, ei row-ta 'Delete' kore diye, "
                            + "shothik product-er upor 'Add Product' form diye restock koro."
            );
        }

        existing.setName(product.getName());
        existing.setCategory(product.getCategory());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setDate(product.getDate());

        productRepository.save(existing);
    }

//    public List<Product> getAll() {
//        return productRepository.findAll();
//    }
public List<Product> getAll() {
    return productRepository.findByActiveTrue();
}

public void deactivateProduct(long id) {
    Product product = productRepository.findById(id)
    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

    product.setActive(false);
        product.setDeactivatedAt(LocalDateTime.now());
        productRepository.save(product);
    }


    public List<Product> search(String name, String category) {
        return productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(name,category);
    }
    public long productCount() {
        return productRepository.count();
    }
    public long inStockCount() {
        return productRepository.countByStockGreaterThan(10);
    }
    public long lowStockCount() {
        return productRepository.countByStockLessThanEqualAndStockGreaterThan(10,0);
    }
    public long outOfStockCount() {
        return productRepository.countByStock(0);
    }
    public Product getById(long id) {
       return productRepository.findById(id).orElse(null);
    }
    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    public List<String> getSuggestedName() {
        return productRepository.findAllDistinctNames();
    }

    // ---------- Soft delete / Restore ----------
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        product.setActive(false);
        product.setDeactivatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    public void restoreProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        product.setActive(true);
        product.setDeactivatedAt(null);
        productRepository.save(product);
    }

    // ---------- Removed products: search + filter (Java Stream, no Specification) ----------
    public List<Product> searchRemovedProducts(String keyword, LocalDate dateFrom, LocalDate dateTo) {
        List<Product> removed = productRepository.findByActiveFalse();

        return removed.stream()
                .filter(p -> keyword == null || keyword.isBlank()
                        || p.getName().toLowerCase().contains(keyword.toLowerCase())
                        || p.getCategory().toLowerCase().contains(keyword.toLowerCase()))
                .filter(p -> dateFrom == null
                        || (p.getDeactivatedAt() != null
                        && !p.getDeactivatedAt().toLocalDate().isBefore(dateFrom)))
                .filter(p -> dateTo == null
                        || (p.getDeactivatedAt() != null
                        && !p.getDeactivatedAt().toLocalDate().isAfter(dateTo)))
                .toList();
    }

    // ---------- Removed products stats ----------
    public RemovedProductStatsDto calculateStats(List<Product> removed) {
        long total = removed.size();

        long thisMonth = removed.stream()
                .filter(p -> p.getDeactivatedAt() != null
                        && p.getDeactivatedAt().getMonth() == LocalDate.now().getMonth()
                        && p.getDeactivatedAt().getYear() == LocalDate.now().getYear())
                .count();

        String mostRecentName = removed.stream()
                .filter(p -> p.getDeactivatedAt() != null)
                .max(Comparator.comparing(Product::getDeactivatedAt))
                .map(Product::getName)
                .orElse("—");

        return new RemovedProductStatsDto(total, thisMonth, mostRecentName);
    }
}

