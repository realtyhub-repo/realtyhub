package service.auth;


import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import service.auth.dto.RolUsuario;
import service.auth.dto.internal.CrearUsuarioRequest;
import service.auth.dto.request.RegisterRequest;
import service.auth.dto.response.UsuarioResponse;
import service.auth.service.JwtService;
import service.auth.service.RefreshTokenService;
import service.auth.service.TokenGenerator;
import service.auth.service.UserServiceClient;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
public class Test {

    private final JwtService jwtService;
    private final TokenGenerator tokenGenerator;
    private final RefreshTokenService refreshTokenService;
    private final UserServiceClient userServiceClient;


    public Test(JwtService jwtService, TokenGenerator tokenGenerator, RefreshTokenService refreshTokenService, UserServiceClient userServiceClient) {
        this.jwtService = jwtService;
        this.tokenGenerator = tokenGenerator;
        this.refreshTokenService = refreshTokenService;
        this.userServiceClient = userServiceClient;
    }

    @GetMapping("/")
    public void test(){
        String token = jwtService.generarAccessToken(UUID.randomUUID(), RolUsuario.CLIENTE);
        log.info(token);

        Claims claims = jwtService.validarToken(token);

        log.info(claims.getSubject());
        log.info(claims.get("rol").toString());

    }

    @GetMapping("/secure")
    public String test2(){


        String token = tokenGenerator.generarTokenCrudo();
        String token_hash = tokenGenerator.hashear(token);
         return token + "\n" + token_hash +"\n" + tokenGenerator.validarHash(token,token_hash);
    }

    @GetMapping("/refresh")
    public String test3(){

        return refreshTokenService.crearRefreshToken(UUID.randomUUID());
    }

    @PostMapping("/crear")
    public UsuarioResponse test4(){


        return userServiceClient.crearUsuario(new CrearUsuarioRequest("Juan@gmail.com","Juan","https"));
    }

    @GetMapping("/{id}")
    public UsuarioResponse test4(@PathVariable UUID id){


        return userServiceClient.buscarUsuarioId(id);
    }

    @PostMapping("/profil")
    public RegisterRequest pass(@RequestBody @Valid RegisterRequest request){
        return request;
    }

}

