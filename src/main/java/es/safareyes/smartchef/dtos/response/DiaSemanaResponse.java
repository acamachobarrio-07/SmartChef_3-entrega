package es.safareyes.smartchef.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record DiaSemanaResponse(

        @Schema(description = "Fecha del día", example = "2026-11-16")
        LocalDate dia,

        @Schema(description = "Nombre del día", example = "lunes")
        String nombreDia,

        @Schema(description = "Calorías totales del día", example = "950")
        Long caloriasTotales,

        @Schema(description = "Comidas registradas ese día (vacía si no hay)")
        List<ComidaResponse> comidas

) {
}
