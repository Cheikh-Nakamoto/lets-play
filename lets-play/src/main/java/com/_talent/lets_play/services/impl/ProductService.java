package com._talent.lets_play.services.impl;

import com._talent.lets_play.dto.ProductDTO;
import com._talent.lets_play.exception.BadRequestException;
import com._talent.lets_play.exception.ResourceNotFoundException;
import com._talent.lets_play.exception.UnauthorizedAccessException;
import com._talent.lets_play.models.Product;
import com._talent.lets_play.models.UserPrincipal;
import com._talent.lets_play.repository.ProductRepository;
import com._talent.lets_play.services.IProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j // Ajoute un logger pour un meilleur suivi
public class ProductService implements IProduct {
    private final ProductRepository productRepo;

    @Override
    @Transactional
    @CacheEvict(value = {"allProducts", "productsByUser"}, allEntries = true)
    public Product addProduct(Product product) {
        if (product == null) {
            throw new BadRequestException("Le produit ne peut pas être null");
        }
        if (product.getUserId() == null || product.getUserId().trim().isEmpty()) {
            throw new BadRequestException("L'ID de l'utilisateur est requis");
        }

        log.info("Ajout d'un nouveau produit par l'utilisateur: {}", product.getUserId());
        return productRepo.save(product);
    }

    public boolean isOwnerOrAdmin(String productId, String userId) {
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getProductbyID(productId)
                .map(product -> product.getUserId().equals(userId))
                .orElse(false) ||
                // Vérifier si l'utilisateur est admin (à adapter selon votre logique)
                user.isAdmin();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "allProducts", allEntries = true),
            @CacheEvict(value = "productById", key = "#productId"),
            @CacheEvict(value = "productsByUser", key = "#userid")
    })
    public void removeProduct(String productId, String userid,String role) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new BadRequestException("L'ID du produit est requis");
        }

        Optional<Product> productOpt = getProductbyID(productId);
        Product product = productOpt.orElseThrow(() ->
                new ResourceNotFoundException("Produit non trouvé avec l'ID: " + productId));

        if (!product.getUserId().equals(userid) && !role.equals("ADMIN")) {
            log.warn("Tentative de suppression non autorisée du produit {} par l'utilisateur {}", productId, userid);
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à supprimer ce produit");
        }

        log.info("Suppression du produit: {} par l'utilisateur: {}", productId, userid);
        productRepo.delete(product);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "allProducts", allEntries = true),
            @CacheEvict(value = "productById", key = "#productId"),
            @CacheEvict(value = "productsByUser", allEntries = true)
    })
    public Product updateProduct(ProductDTO productDTO, String productId) {
        // Validation des paramètres d'entrée
        if (productDTO == null) {
            throw new BadRequestException("Les données du produit ne peuvent pas être null");
        }
        if (productId == null || productId.trim().isEmpty()) {
            throw new BadRequestException("L'ID du produit est requis");
        }

        // Récupérer le produit existant
        Product existingProduct = getProductbyID(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + productId));

        // Mettre à jour uniquement les champs non-nuls du DTO
        updateProductFromDto(productDTO, existingProduct);

        log.info("Mise à jour du produit: {}", productId);
        return productRepo.save(existingProduct);
    }

    private void updateProductFromDto(ProductDTO dto, Product existingProduct) {
        if (dto.getName() != null) {
            existingProduct.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existingProduct.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            existingProduct.setPrice(BigDecimal.valueOf(dto.getPrice()));
        }
    }
    @Override
    @Cacheable(value = "productById", key = "#productId", unless = "#result == null")
    public Optional<Product> getProductbyID(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new BadRequestException("L'ID du produit est requis");
        }

        log.debug("Récupération du produit avec l'ID: {}", productId);
        Optional<Product> product = productRepo.findById(productId);

        if (product.isEmpty()) {
            log.debug("Produit non trouvé avec l'ID: {}", productId);
        }

        return product;
    }

    @Override
    @Cacheable(value = "allProducts")
    public List<Product> getAllProducts() {
        try {
            log.debug("Récupération de tous les produits");
            return productRepo.findAll();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de tous les produits", e);
            throw new RuntimeException("Erreur lors de la récupération des produits: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "productsByUser", key = "#userID", unless = "#result.isEmpty()")
    public List<Product> getProductsbyUserid(String userID) {
        if (userID == null || userID.trim().isEmpty()) {
            throw new BadRequestException("L'ID de l'utilisateur est requis");
        }

        log.debug("Récupération des produits pour l'utilisateur: {}", userID);
        return productRepo.findProductByUserId(userID);
    }
}
