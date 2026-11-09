package com.karolsol.nabu.auth.internal;

import com.karolsol.nabu.auth.LoginUserCommand;
import com.karolsol.nabu.auth.internal.exception.InvalidJwtTokenException;
import com.karolsol.nabu.user.UserFacade;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserFacade userFacade;

  public AuthTokens login(LoginUserCommand command) {
    Authentication authentication =
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                command.email(), command.password()));

    UserDetails user = (UserDetails) authentication.getPrincipal();
    if (user == null) {
      throw new InvalidJwtTokenException();
    }

    return tokensFor(parseUserId(user.getUsername()));
  }

  public AuthTokens refresh(String token) {
    try {
      Jwt jwt = jwtService.decodeToken(token);
      String subject = jwt.getSubject();
      if (subject == null || !userFacade.existById(UUID.fromString(subject))) {
        throw new InvalidJwtTokenException();
      }
      return tokensFor(parseUserId(subject));
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidJwtTokenException();
    }
  }

  private AuthTokens tokensFor(UUID userId) {
    return new AuthTokens(
        jwtService.generateAccessToken(userId), jwtService.generateRefreshToken(userId));
  }

  private UUID parseUserId(String value) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw new InvalidJwtTokenException();
    }
  }

  public record AuthTokens(String accessToken, String refreshToken) {}
}
