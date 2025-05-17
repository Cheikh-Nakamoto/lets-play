package com._talent.lets_play.controllers;
import com._talent.lets_play.models.UserPrincipal;
import com._talent.lets_play.services.impl.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/products")
@RequiredArgsConstructor
public class ProductControllers {
    private final ProductService productService;

    @GetMapping(path = "/GetAllProducts")
    public Iterable<com._talent.lets_play.models.Product> getAllProducts(){
        return productService.getAllProducts();
    }

    @GetMapping(path = "/GetProductbyId")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public com._talent.lets_play.models.Product getProductbyId(@RequestParam String productId){
        return productService.getProductbyID(productId).get();
    }

    @GetMapping(path = "/GetProductsByUserId")
    public Iterable<com._talent.lets_play.models.Product> getProductsByUserId(@RequestParam String userid){
        return productService.getProductsbyUserid(userid);
    }

    @PostMapping(path = "/AddProduct")
    public com._talent.lets_play.models.Product addProduct(@RequestBody com._talent.lets_play.models.Product product) {
        return productService.addProduct(product);
    }

    @DeleteMapping(path = "/RemoveProduct/{productId}")
    public void removeProduct(@PathVariable String productId){
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Addproduct: " + user.getId());
        productService.removeProduct(productId, user.getId());
    }

    @PutMapping(path = "/UpdateProduct/{productId}")
    public com._talent.lets_play.models.Product updateProduct(@RequestBody com._talent.lets_play.models.Product product, @PathVariable String productId){
        return productService.updateProduct(product, productId);
    }
}