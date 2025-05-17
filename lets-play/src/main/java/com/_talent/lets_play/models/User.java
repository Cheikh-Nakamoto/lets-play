package com._talent.lets_play.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document()
public class User {
    @Id
    String id;
    @Field
    String name;
    @Field
    String email;
    @Field
    String password;
    String role;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    List<Product> products;
}
