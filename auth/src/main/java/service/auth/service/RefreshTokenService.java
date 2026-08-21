package service.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import service.auth.dto.response.AuthResponse;
import service.auth.dto.response.UsuarioResponse;
import service.auth.entity.RefreshToken;
import service.auth.repository.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserServiceClient userServiceClient;
    private final TokenGenerator tokenGenerator;

    private final JwtService jwtService;

    public String crearRefreshToken(UUID userId){

        String tokenPlano = tokenGenerator.generarTokenCrudo();
        String tokenHash = tokenGenerator.hashear(tokenPlano);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expireAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenPlano;

    }

    public RefreshToken validarRefreshToken(String tokenCrudo){

        String refreshTokenHash = tokenGenerator.hashear(tokenCrudo);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(refreshTokenHash)
                .orElseThrow(() ->
                        new RuntimeException("Token no existente"));



        if(refreshToken.getRevoked()){
            throw new RuntimeException("Token revocado");
        }

        if(refreshToken.getExpireAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token expirado");
        }

        return refreshToken;
    }


    public AuthResponse rotarRefreshToken(String tokenCrudo){

        RefreshToken tokenValido = validarRefreshToken(tokenCrudo);
        UsuarioResponse usuarioResponse = userServiceClient.buscarUsuarioId(tokenValido.getUserId());

        tokenValido.setRevoked(true);
        refreshTokenRepository.saveAndFlush(tokenValido);
        String nuevoRefreshToken = crearRefreshToken(tokenValido.getUserId());
        String nuevoAccessToken = jwtService.generarAccessToken(
                tokenValido.getUserId(),
                usuarioResponse.rolUsuario()
        );

        return new AuthResponse(nuevoAccessToken,nuevoRefreshToken);
    }

}
