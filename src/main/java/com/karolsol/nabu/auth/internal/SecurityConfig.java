package com.karolsol.nabu.auth.internal;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, @Qualifier("jwtDecoder") JwtDecoder jwtDecoder) {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/logout")
                    .authenticated()
                    .requestMatchers("/api/v1/auth/**", "/api/v1/users")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(jwt -> jwt.decoder(jwtDecoder))
                    .bearerTokenResolver(cookieBearerTokenResolver()));
    return http.build();
  }

  private BearerTokenResolver cookieBearerTokenResolver() {
    return request -> {
      if ("/api/v1/auth/refresh".equals(request.getServletPath())) {
        return null;
      }
      if (request.getCookies() != null) {
        return Arrays.stream(request.getCookies())
            .filter(cookie -> "access_token".equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
      }
      return null;
    };
  }

  @Bean
  SecretKey jwtSecretKey(JwtProperties props) {
    return new SecretKeySpec(Base64.getDecoder().decode(props.secret()), "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKey key) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKey key) {
    return jwtDecoder(key, JwtTokenType.ACCESS);
  }

  @Bean
  JwtDecoder refreshJwtDecoder(SecretKey key) {
    return jwtDecoder(key, JwtTokenType.REFRESH);
  }

  private JwtDecoder jwtDecoder(SecretKey key, JwtTokenType tokenType) {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();

    OAuth2TokenValidator<Jwt> defaultWithClaim =
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefault(),
            new JwtClaimValidator<String>("type", tokenType.toString()::equals));

    decoder.setJwtValidator(defaultWithClaim);
    return decoder;
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) {
    return cfg.getAuthenticationManager();
  }
}
