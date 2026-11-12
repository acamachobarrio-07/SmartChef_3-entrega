package es.safareyes.smartchef.dtos.response;

import java.time.LocalDate;

public record SemanaCaloriasResponse(
        LocalDate dia,
        String nombreDia,
        Long totalRecetasCocinadas,
        Long caloriasTotales
) {
}
