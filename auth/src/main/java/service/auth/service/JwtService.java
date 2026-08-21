package service.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import service.auth.dto.RolUsuario;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private static final long ACCESS_TOKEN_EXPIRATION = 100*60*15;

    public String generarAccessToken(UUID userId, RolUsuario rol){

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("rol",rol.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRATION)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();

    }


    public Claims validarToken(String token){
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }



}
