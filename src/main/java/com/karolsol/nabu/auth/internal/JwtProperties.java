package com.karolsol.nabu.auth.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("jwt")
public record JwtProperties(
    @NotBlank @Size(min = 43) String secret,
    @NotNull Duration accessTtl,
    @NotNull Duration refreshTtl,
    @DefaultValue("true") boolean secureCookie) {}
