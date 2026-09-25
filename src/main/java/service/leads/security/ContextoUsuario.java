package service.leads.security;

import service.leads.dto.internal.RolUsuario;

import java.util.UUID;

public record ContextoUsuario(UUID userId, RolUsuario rol) {}