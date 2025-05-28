package com._talent.lets_play.services.impl;


import com._talent.lets_play.models.Product;
import com._talent.lets_play.models.UserPrincipal;
import com._talent.lets_play.services.IAuthorize;
import org.springframework.stereotype.Service;

// Service d'autorisation personnalisé
@Service("productAuthorizationService")
public  class ProductAuthorizationService implements IAuthorize {
    private ProductService productService;

    public boolean canModifyProduct(Product product, UserPrincipal user) {
        if (product == null || user == null) {
            return false;
        }
        return product.getUserId().equals(user.getId()) ||
                user.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
    // Nouvelle méthode pour @PreAuthorize
    public boolean canModifyProductById(String productId, UserPrincipal user) {
        if (productId == null || user == null) {
            return false;
        }

        Product product = productService.getProductbyID(productId).orElse(null);
        if (product == null) {
            return false; // Le produit n'existe pas
        }

        return canModifyProduct(product, user);
    }
}
