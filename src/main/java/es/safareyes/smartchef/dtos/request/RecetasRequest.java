
package es.safareyes.smartchef.dtos.request;

import es.safareyes.smartchef.entity.ERecetaDificultad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record RecetasRequest(

        @Schema(description = "Tiempo máximo en minutos", example = "30")
        @Positive(message = "El tiempo debe ser mayor que 0")
        Integer tiempoMax,

        @Schema(description = "Dificultad de la receta", example = "FACIL")
        ERecetaDificultad dificultad,

        @Schema(description = "Filtrar recetas vegetarianas", example = "true")
        Boolean vegetariano,

        @Schema(description = "Filtrar recetas sin gluten", example = "true")
        Boolean sinGluten,


        @Schema(description = "Filtrar recetas rápidas (30 minutos o menos)", example = "true"
        )
        Boolean rapido,


        @Schema(description = "Filtrar recetas con coste de 2,50 euros o menos por ración", example = "true"
        )
        Boolean economico,



        @Schema(description = "Número de página, empezando en 0", example = "0")
        @Min(value = 0, message = "La página no puede ser negativa")
        Integer page,

        @Schema(description = "Número de recetas por página", example = "10")
        @Min(value = 1, message = "El tamaño debe ser al menos 1")
        Integer size

) {
}
