package es.safareyes.smartchef.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record SemanaRequest(

        @Schema(description = "ID del usuario", example = "2")
        @NotNull(message = "El ID del usuario es obligatorio")
        @Positive(message = "El ID del usuario debe ser positivo")
        Integer idUsuario,

        @Schema(description = "Cualquier día de la semana a consultar (si no se indica, hoy)", example = "2026-11-18")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fecha

) {
}
