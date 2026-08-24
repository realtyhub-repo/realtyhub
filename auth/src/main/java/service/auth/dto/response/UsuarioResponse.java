package service.auth.dto.response;

import service.auth.enums.RolUsuario;

import java.util.UUID;

public record UsuarioResponse(UUID id, RolUsuario rolUsuario){
}
