package service.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import service.auth.dto.internal.CrearUsuarioRequest;
import service.auth.dto.response.UsuarioResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestClient restClient;


    public UsuarioResponse crearUsuario(CrearUsuarioRequest request){


        return restClient.post()
                .uri("/usuario")
                .body(request)
                .retrieve()
                .body(UsuarioResponse.class);

    }

    public UsuarioResponse buscarUsuarioId(UUID usuarioId){
        return restClient.get()
                .uri("/usuario/{id}",usuarioId)
                .retrieve()
                .body(UsuarioResponse.class);
    }


}
