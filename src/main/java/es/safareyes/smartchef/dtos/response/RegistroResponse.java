package es.safareyes.smartchef.dtos.response;

import es.safareyes.smartchef.entity.ECocinaTipo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record RegistroResponse(

        @Schema(description = "ID del registro", example = "50")
        Integer id,

        @Schema(description = "Fecha y hora de la comida", example = "2026-11-24T14:00:00")
        LocalDateTime fecha,

        @Schema(description = "Momento de la comida", example = "ALMUERZO")
        ECocinaTipo tipo,

        @Schema(description = "ID de la receta", example = "33")
        Integer idReceta,

        @Schema(description = "Nombre de la receta", example = "Gazpacho casero")
        String receta,

        @Schema(description = "Calorías de la receta", example = "120")
        Integer calorias,

        @Schema(description = "Si la receta está retirada (RN-09)", example = "false")
        Boolean recetaRetirada
) {
}