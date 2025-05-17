package com._talent.lets_play.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    String id;
    @Field
    String name;
    @Field
    String description;
    @Field
    @Positive
    Double price;
    @Field
    String userId;
}
