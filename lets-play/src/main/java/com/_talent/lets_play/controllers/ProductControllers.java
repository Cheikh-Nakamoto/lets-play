package com._talent.lets_play.controllers;

import com._talent.lets_play.dto.ProductDTO;
import com._talent.lets_play.exception.BadRequestException;
import com._talent.lets_play.exception.ResourceNotFoundException;
import com._talent.lets_play.models.Product;
import com._talent.lets_play.models.UserPrincipal;
import com._talent.lets_play.services.IProduct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Product Management", description = "APIs for managing products including CRUD operations and user-specific product management")
@SecurityRequirement(name = "bearerAuth")
public class ProductControllers {

    private final IProduct productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a list of all products available in the system. This endpoint is accessible to all users.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))), @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Internal server error\" }")))})
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Récupération de tous les produits");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID. Requires USER or ADMIN role.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Product found and retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))), @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Produit non trouvé avec l'ID: productId\" }"))), @ApiResponse(responseCode = "403", description = "Access denied - USER or ADMIN role required", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Access denied\" }"))), @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Authentication required\" }")))})
    public ResponseEntity<Product> getProductById(@Parameter(description = "ID of the product to retrieve", example = "60f7b3b3b3b3b3b3b3b3b3b3") @PathVariable String productId) {

        log.info("Récupération du product avec l'ID: {}", productId);
        return productService.getProductbyID(productId).map(ResponseEntity::ok).orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + productId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get products by user ID", description = "Retrieve all products belonging to a specific user. This endpoint is accessible to all users.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))), @ApiResponse(responseCode = "400", description = "Bad request - Invalid user ID", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"L'ID utilisateur ne peut pas être vide\" }"))), @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Internal server error\" }")))})
    public ResponseEntity<List<Product>> getProductsByUserId(@Parameter(description = "ID of the user whose products to retrieve", example = "60f7b3b3b3b3b3b3b3b3b3b3") @PathVariable String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("L'ID utilisateur ne peut pas être vide");
        }
        log.info("Récupération des produits pour l'utilisateur: {}", userId);
        return ResponseEntity.ok(productService.getProductsbyUserid(userId));
    }

    @PostMapping()
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a new product", description = "Add a new product to the system. The product will be associated with the authenticated user. Requires USER role.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))), @ApiResponse(responseCode = "400", description = "Bad request - Invalid product data", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Invalid product data\" }"))), @ApiResponse(responseCode = "403", description = "Access denied - USER role required", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Access denied\" }"))), @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Authentication required\" }")))})
    public ResponseEntity<Product> addProduct(@Parameter(description = "Product information to create") @Valid @RequestBody Product product) {

        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Si l'utilisateur est admin, on peut assigner le produit à "ADMIN"
        if (user != null && user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
            product.setUserId("ADMIN");
        } else {
            assert user != null;
            product.setUserId(user.getId());
        }

        log.info("Ajout d'un nouveau produit par l'utilisateur: {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(product));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Delete a product", description = "Delete a product from the system. Users can only delete their own products, while admins can delete any product. Requires USER or ADMIN role.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Product deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"message\": \"Product delete successfuly !\" }"))), @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Product not found\" }"))), @ApiResponse(responseCode = "403", description = "Access denied - Cannot delete other users' products or insufficient permissions", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Access denied\" }"))), @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Authentication required\" }")))})
    public ResponseEntity<String> removeProduct(@Parameter(description = "ID of the product to delete", example = "60f7b3b3b3b3b3b3b3b3b3b3") @PathVariable String productId) {

        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = user.getAuthorities().stream().findFirst().orElseThrow().getAuthority();

        log.info("Tentative de suppression du produit: {} par l'utilisateur: {} {}", productId, user.getId(), user.getAuthorities());

        productService.removeProduct(productId, user.getId(), role);
        return ResponseEntity.status(HttpStatus.OK).body("Product delete successfuly !");
    }

    @PutMapping("/{productId}")
    @PreAuthorize("@productService.isOwnerOrAdmin(#productId, authentication.principal.id)")
    @Operation(summary = "Update a product", description = "Update an existing product. Users can only update their own products, while admins can update any product. Requires ownership of the product or ADMIN role.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))), @ApiResponse(responseCode = "400", description = "Bad request - Invalid product data", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Invalid product data\" }"))), @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Produit non trouvé avec l'ID: productId\" }"))), @ApiResponse(responseCode = "403", description = "Access denied - Cannot update other users' products or insufficient permissions", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Access denied\" }"))), @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Authentication required\" }")))})
    public ResponseEntity<Product> updateProduct(@Parameter(description = "Updated product information") @Valid @RequestBody ProductDTO product,

                                                 @Parameter(description = "ID of the product to update", example = "60f7b3b3b3b3b3b3b3b3b3b3") @PathVariable String productId) {

        productService.getProductbyID(productId).orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + productId));

        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Mise à jour du produit: {} par l'utilisateur: {}", productId, user.getId());

        return ResponseEntity.ok(productService.updateProduct(product, productId));
    }
}