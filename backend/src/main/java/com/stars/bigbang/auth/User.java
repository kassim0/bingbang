package com.stars.bigbang.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class User {

    @Id
    private Long id;
    private String nom;
    private String prenom;
    private String email;
}
