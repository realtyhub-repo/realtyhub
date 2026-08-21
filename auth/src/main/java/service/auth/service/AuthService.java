package service.auth.service;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import service.auth.dto.Proveedor;
import service.auth.dto.internal.CrearUsuarioRequest;
import service.auth.dto.internal.Usuario;
import service.auth.dto.request.RegisterRequest;
import service.auth.dto.response.AuthResponse;
import service.auth.dto.response.UsuarioResponse;
import service.auth.entity.AuthProvider;
import service.auth.repository.AuthProviderRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthProviderRepository authProviderRepository;
    private final GoogleAuthService googleAuthService;
    private final UserServiceClient userServiceClient;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public AuthResponse loginConGoogle(String idToken){
        Usuario usuario = procesarLoginGoogle(idToken);
        return generarTokensPara(usuario);
    }

    public AuthResponse loginLocal(String email, String passwordPlano){
        Usuario usuario = procesarLoginLocal(email, passwordPlano);


        return generarTokensPara(usuario);
    }
    /*
    public AuthResponse registrar(RegisterRequest request){

        Optional<AuthProvider> authProvider = authProviderRepository.findByEmail(request.email());

        if(authProvider.isPresent()){
            throw new RuntimeException("Este correo esta registrado");
        }

        String passwordHash = passwordEncoder.encode(request.password());

    }*/

    /*
    * Estas funciones son privadas
    * porque solo se usaran a nivel de clase y no deberian
    *  ser usadas en isntancias de dicha clase
    * */

    private Usuario procesarLoginLocal(String email, String passwordPlano){

        AuthProvider authProvider = authProviderRepository.findByEmail(email)
                //Esta excepción debe ser personalizada
                .orElseThrow(()->new RuntimeException("Email o contraseña incorrectos"));

        if(authProvider.getProveedor()==Proveedor.GOOGLE){
            //Esta excepción debe ser personalizada
            throw new RuntimeException("Este correo esta registrado con Google");
        }

        if(!passwordEncoder.matches(passwordPlano, authProvider.getPasswordHash())){
            //Esta excepción debe ser personalizada
            throw new RuntimeException("Este email o contraseña incorrecta");

        }

        return construirUsuarioDesde(authProvider);
    }

    private Usuario procesarLoginGoogle(String idToken){
        Payload payload = googleAuthService.verificarToken(idToken);
        String googleId = payload.getSubject();
        String email    = payload.getEmail();

        Optional<AuthProvider> porGoogleId = authProviderRepository.findByGoogleId(googleId);

        if(porGoogleId.isPresent()){
            return construirUsuarioDesde(porGoogleId.get());
        }

        Optional<AuthProvider> porEmail= authProviderRepository.findByEmail(email);
        if(porEmail.isPresent()){
            throw new RuntimeException("Este correo ya esta registrado con "+porEmail.get().getProveedor());

        }

        return crearUsuarioNuevoConGoogle(payload);
    }

    private AuthResponse generarTokensPara(Usuario usuario){
        String refreshToken = refreshTokenService.crearRefreshToken(usuario.id());
        String accessToken  = jwtService.generarAccessToken(usuario.id(), usuario.rol());
        return new AuthResponse(
                accessToken,
                refreshToken
        );
    }


    private Usuario crearUsuarioNuevoConGoogle(Payload payload){

        CrearUsuarioRequest request = new CrearUsuarioRequest(
                payload.getEmail(),
                (String) payload.get("name"),
                (String)payload.get("picture")
        );

        UsuarioResponse usuarioResponse = userServiceClient.crearUsuario(request);

        AuthProvider authProvider = AuthProvider.builder()
                .userId(usuarioResponse.id())
                .proveedor(Proveedor.GOOGLE)
                .email(payload.getEmail())
                .googleId(payload.getSubject())
                .passwordHash(null)
                .build();

        authProviderRepository.save(authProvider);
        return new Usuario(usuarioResponse.id(), usuarioResponse.rolUsuario());
    }

    private Usuario construirUsuarioDesde(AuthProvider authProvider){
        UsuarioResponse usuarioResponse = userServiceClient.buscarUsuarioId(authProvider.getUserId());

        return new Usuario(
                usuarioResponse.id(),
                usuarioResponse.rolUsuario()
        );
    }




}
