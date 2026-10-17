package com.stars.bigbang.dto.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max=72,message = "le nombre de caractère du mot de passe doit être compris entre 8 et 72")
        String password
) {
}
