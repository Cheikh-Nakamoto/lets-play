package com._talent.lets_play.services;

import com._talent.lets_play.models.Product;

import java.util.List;
import java.util.Optional;

public interface IProduct {
    Product addProduct(Product product);
    void removeProduct(String productId,String userid,String role);
    Product updateProduct(Product product,String productId);
    Optional<Product> getProductbyID(String productId);
    List<Product> getAllProducts();
    List<Product> getProductsbyUserid(String userID);
}
