package com._talent.lets_play.controllers;
import com._talent.lets_play.exception.BadRequestException;
import com._talent.lets_play.exception.ResourceNotFoundException;
import com._talent.lets_play.exception.UnauthorizedAccessException;
import com._talent.lets_play.models.Product;
import com._talent.lets_play.models.UserPrincipal;
import com._talent.lets_play.services.impl.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/products")
@RequiredArgsConstructor
@Slf4j
@Validated
//@Tag(name = "Produits", description = "API des opérations sur les produits")
public class ProductControllers {
    private final ProductService productService;

    @GetMapping

    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Récupération de tous les produits");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")

    public ResponseEntity<Product> getProductById(@PathVariable String productId) {
        log.info("Récupération du produit avec l'ID: {}", productId);
        return productService.getProductbyID(productId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + productId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Product>> getProductsByUserId(@PathVariable String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("L'ID utilisateur ne peut pas être vide");
        }
        log.info("Récupération des produits pour l'utilisateur: {}", userId);
        return ResponseEntity.ok(productService.getProductsbyUserid(userId));
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Product> addProduct(@Valid @RequestBody Product product){
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user != null && user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
            product.setUserId("ADMIN");
        }
        product.setUserId(user.getId());
        log.info("Ajout d'un nouveau produit par l'utilisateur: {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(product));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> removeProduct(@PathVariable String productId) {
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = user.getAuthorities().stream().findFirst().orElseThrow().getAuthority();
        log.info("Tentative de suppression du produit: {} par l'utilisateur: {}", productId, user.getAuthorities());
        productService.removeProduct(productId, user.getId(),role);
        return ResponseEntity.status(HttpStatus.OK).body("Product delete successfuly !");
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@Valid @RequestBody Product product, @PathVariable String productId) {
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Vérifier si l'utilisateur est autorisé à modifier ce produit
        Product existingProduct = productService.getProductbyID(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + productId));

        if (!existingProduct.getUserId().equals(user.getId())) {
            log.warn("Tentative de modification non autorisée du produit {} par l'utilisateur {}", productId, user.getId());
            throw new BadRequestException("Vous n'êtes pas autorisé à modifier ce produit");
        }

        log.info("Mise à jour du produit: {} par l'utilisateur: {}", productId, user.getId());
        return ResponseEntity.ok(productService.updateProduct(product, productId));
    }
}