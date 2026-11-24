package es.safareyes.smartchef.dtos.request;

import es.safareyes.smartchef.entity.ECocinaTipo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record RegistroRequest(

        @Schema(description = "ID de la receta cocinada", example = "33")
        @NotNull(message = "La receta es obligatoria")
        @Positive(message = "El ID de la receta debe ser positivo")
        Integer idReceta,

        @Schema(description = "Momento de la comida: DESAYUNO, ALMUERZO o CENA", example = "ALMUERZO")
        @NotNull(message = "El tipo de comida es obligatorio (DESAYUNO, ALMUERZO o CENA)")
        ECocinaTipo tipo,

        @Schema(description = "Fecha y hora de la comida (no puede ser futura)", example = "2026-11-24T14:00:00")
        @NotNull(message = "La fecha es obligatoria")
        @PastOrPresent(message = "No se pueden registrar comidas en fechas futuras")
        LocalDateTime fecha,

        @Schema(description = "Si es true, resta de la despensa los ingredientes de la receta", example = "false")
        Boolean descontarDespensa
) {
}
