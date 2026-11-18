package es.safareyes.smartchef.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UsuarioRequest(
        @NotNull(message = "El ID del usuario es obligatorio")
        @Positive(message = "El ID del usuario debe ser positivo")
        Integer idUsuario
) {
}
