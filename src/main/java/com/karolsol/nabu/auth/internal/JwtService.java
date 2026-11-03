package com.karolsol.nabu.auth.internal;

import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtEncoder encoder;

  private final JwtDecoder refreshJwtDecoder;

  private final JwtProperties props;

  public JwtService(
      JwtEncoder encoder,
      @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder,
      JwtProperties props) {
    this.encoder = encoder;
    this.refreshJwtDecoder = refreshJwtDecoder;
    this.props = props;
  }

  public String generateAccessToken(UUID username) {
    return generateToken(username, JwtTokenType.ACCESS);
  }

  public String generateRefreshToken(UUID username) {
    return generateToken(username, JwtTokenType.REFRESH);
  }

  public Jwt decodeToken(String token) {
    return refreshJwtDecoder.decode(token);
  }

  private String generateToken(UUID id, JwtTokenType type) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plus(props.accessTtl()))
            .subject(String.valueOf(id))
            .claim("type", type.toString())
            .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
