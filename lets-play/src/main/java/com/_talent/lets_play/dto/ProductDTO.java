package com._talent.lets_play.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    @Schema(
            description = "Product name",
            example = "iPhone 14 Pro",
            minLength = 2,
            maxLength = 100
    )
    @Field
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
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private Double price;
}
