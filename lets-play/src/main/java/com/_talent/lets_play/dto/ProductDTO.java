package com._talent.lets_play.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @Positive
    private Double price;
    @NotBlank
    private String userId;
}
