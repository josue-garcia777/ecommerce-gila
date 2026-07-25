package com.josue.ecommerce.identity.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.identity.bootstrap-admin")
@Validated
public record InitAdminProperties(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 1, max = 72) String password
) {
}
