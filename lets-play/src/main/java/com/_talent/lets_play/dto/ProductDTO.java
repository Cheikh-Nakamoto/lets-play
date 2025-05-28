package com._talent.lets_play.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    @Field
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    @Field
    @Size(min = 10, max = 100, message = "description must be between 2 and 100 characters")
    private String description;
    @Positive
    private Double price;
}
