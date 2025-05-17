package com._talent.lets_play.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document()
public class Product {
    @Id
    private String id;
    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom du produit doit contenir entre 2 et 100 caractères")
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @PositiveOrZero(message = "Le prix doit être supérieur ou égal à zéro")
    private BigDecimal price;

    private String userId;

}
