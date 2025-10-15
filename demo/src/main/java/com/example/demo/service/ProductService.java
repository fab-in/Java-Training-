package com.example.demo.service;

import com.example.demo.model.Product;
import java.util.*;
import java.util.Arrays;

import org.springframework.stereotype.Component;

@Component
public class ProductService {
    List<Product> products = new ArrayList<>(Arrays.asList(
            new Product(1, "Laptop", 80000),
            new Product(2, "Smartphone", 50000),
            new Product(3, "Tablet", 30000)));

    public List<Product> getProducts() {

        return products;
    }

    public Product getProductById(int prodId) {
        for (Product p : products) {
            if (p.getProdId() == prodId) {
                return p;
            }
        }
        return null;
    }

    public List<Product> addProduct(Product product) {
        products.add(product);
        return products;
    }

    public List<Product> updateProduct(Product product) {
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            if (p.getProdId() == product.getProdId()) {
                products.set(i, product);
                return products;
            }
        }
        return products;
    }

    public void deleteProduct(int prodId) {
        products.removeIf(p -> p.getProdId() == prodId);
    }
}
