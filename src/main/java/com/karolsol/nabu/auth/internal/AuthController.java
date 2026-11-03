package com.karolsol.nabu.auth.internal;

import com.karolsol.nabu.auth.LoginUserCommand;
import com.karolsol.nabu.auth.internal.exception.InvalidJwtTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final AuthCookieService cookieService;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody @Valid LoginUserCommand command) {
    return tokenResponse(authService.login(command), "Login successful");
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(HttpServletRequest request) {
    return tokenResponse(
        authService.refresh(
            cookieService
                .extractCookie(request, "refresh_token")
                .orElseThrow(InvalidJwtTokenException::new)),
        "Token refreshed successfully");
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout() {
    ResponseCookie accessCookie = cookieService.deleteCookie("access_token", "/");
    ResponseCookie refreshCookie =
        cookieService.deleteCookie("refresh_token", "/api/v1/auth/refresh");

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(Map.of("message", "Logged out successfully"));
  }

  private ResponseEntity<?> tokenResponse(AuthService.AuthTokens tokens, String message) {
    ResponseCookie accessCookie = cookieService.createAccessTokenCookie(tokens.accessToken());
    ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(tokens.refreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(Map.of("message", message));
  }
}
