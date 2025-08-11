package com.example.bankcards.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTCore {
    @Value("${app.security.jwt.secret}")
    private String secret;
    @Value("${app.security.jwt.expiration}")
    private int lifeTime;

    public String generateToken(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UsersDetailsImpl usersDetails = (UsersDetailsImpl) authentication.getPrincipal();

        Date expirationDate = Date.from(ZonedDateTime.now().toInstant().plusMillis(lifeTime));

        return JWT.create()
                .withSubject("User details")
                .withIssuer("dev")
                .withClaim("username",usersDetails.getUsername())
                .withClaim("roles",roles)
                .withIssuedAt(new Date())
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

public String validateTokenAndRetrieveClaim(String token) throws JWTVerificationException {
    JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
            .withSubject("User details")
            .withIssuer("dev")
            .build();

    DecodedJWT jwt = verifier.verify(token);
    return jwt.getClaim("username").asString();
}

}
