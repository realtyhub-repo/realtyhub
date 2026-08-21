package service.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdToken.Payload verificarToken(String idTokenString){
        try{
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken==null){
                throw new InvalidBearerTokenException("Token de Google invalido");
            }
            return idToken.getPayload();
        }catch (GeneralSecurityException | IOException e){
            throw new InvalidBearerTokenException("Error verificacion token ");
        }

    }


}
