package es.safareyes.smartchef.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record InformeRecetasResponse(

        @Schema(description = "Fecha inicial del informe", example = "2026-11-01")
        LocalDate desde,

        @Schema(description = "Fecha final del informe", example = "2026-11-30")
        LocalDate hasta,

        @Schema(description = "Top 10 de recetas más cocinadas entre las fechas")
        List<RecetaPopularResponse> masCocinadas,

        @Schema(description = "Top 10 de recetas más guardadas en colecciones")
        List<RecetaPopularResponse> masGuardadas

) {
}
