package com.karolsol.nabu.auth.internal;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthCookieService {
  private JwtProperties jwtProperties;

  public ResponseCookie createAccessTokenCookie(String token) {
    return buildCookie("access_token", token, "/", jwtProperties.accessTtl());
  }

  public ResponseCookie createRefreshTokenCookie(String token) {
    return buildCookie("refresh_token", token, "/api/v1/auth/refresh", jwtProperties.refreshTtl());
  }

  public ResponseCookie deleteCookie(String cookieName, String path) {
    return buildCookie(cookieName, "", path, Duration.ZERO);
  }

  public Optional<String> extractCookie(HttpServletRequest request, String cookieName) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(cookie -> cookieName.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  private ResponseCookie buildCookie(String name, String value, String path, Duration maxAge) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(jwtProperties.secureCookie())
        .path(path)
        .maxAge(maxAge)
        .sameSite("Strict")
        .build();
  }
}
