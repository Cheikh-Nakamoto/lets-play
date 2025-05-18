package services;

import java.util.List;
import java.util.Optional;
import models.product;

public interface ProductService {
    List<product> findAll();
    Optional<product> findById(String id);
    product save(product product);
    Optional<product> update(String id, product product);
    boolean delete(String id);
}