package service.auth.dto.internal;

import service.auth.enums.RolUsuario;

import java.util.UUID;

public record Usuario(
        UUID id,
        RolUsuario rol
) {}