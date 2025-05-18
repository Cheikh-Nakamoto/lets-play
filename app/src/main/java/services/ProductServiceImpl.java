package services;

import models.product;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final List<product> products = new ArrayList<>();

    @Override
    public List<product> findAll() {
        return products;
    }

    @Override
    public Optional<product> findById(String id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    @Override
    public product save(product product) {
        products.add(product);
        return product;
    }

    @Override
    public Optional<product> update(String id, product product) {
        Optional<product> existingProduct = findById(id);
        if (existingProduct.isPresent()) {
            product existing = existingProduct.get();
            existing = product; //replace with the new product
            return Optional.of(existing);
        }
        return Optional.empty();
    }

    @Override
    public boolean delete(String id) {
        Optional<product> productToDelete = findById(id);
        if (productToDelete.isPresent()) {
            products.remove(productToDelete.get());
            return true;
        }
        return false;
    }
}