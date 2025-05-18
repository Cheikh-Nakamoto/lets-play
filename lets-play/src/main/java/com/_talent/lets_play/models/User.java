package com._talent.lets_play.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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


    @Data
    public static class Builder {
        @Value("${admin.username:admin}")
        private String adminUsername;

        @Value("${admin.password:Zone01_Dakar.sn}")
        private String adminPassword;

        @Value("${admin.email:admin@system.com}")
        private String adminEmail;

        @Value("${admin.id:999}")
        private String adminId;

        private String role;
        @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
        List<Product> products;

        public User build() {
            return new User(
                    adminId,
                    adminUsername,
                    adminEmail,
                    adminPassword,
                    "ADMIN",
                    products
            );
        }

    }
}
