package es.safareyes.smartchef.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record SemanaResponse(

        @Schema(description = "Lunes de la semana", example = "2026-11-16")
        LocalDate desde,

        @Schema(description = "Domingo de la semana", example = "2026-11-22")
        LocalDate hasta,

        @Schema(description = "Suma de las calorías de los 7 días", example = "1850")
        Long caloriasSemana,

        @Schema(description = "Los 7 días de la semana, de lunes a domingo")
        List<DiaSemanaResponse> dias

) {
}
