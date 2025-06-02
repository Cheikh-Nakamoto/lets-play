package com._talent.lets_play.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document()
public class Product {

    @Id
    @Schema(description = "Unique identifier of the product", example = "60f7b3b3b3b3b3b3b3b3b3b3", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(
            description = "Product name",
            example = "iPhone 14 Pro",
            minLength = 2,
            maxLength = 100
    )
    @Field
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @Schema(
            description = "Product description",
            example = "Latest iPhone with advanced camera system and A16 Bionic chip",
            maxLength = 500
    )
    @Field
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Schema(
            description = "Product price in currency units",
            example = "999.99",
            minimum = "0"
    )
    @Field
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private Double price;

    private String userId;

}