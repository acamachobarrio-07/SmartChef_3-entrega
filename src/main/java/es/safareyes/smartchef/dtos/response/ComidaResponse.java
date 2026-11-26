package es.safareyes.smartchef.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ComidaResponse(

        @Schema(description = "ID del registro", example = "3")
        Integer id,

        @Schema(description = "Momento del día: DESAYUNO, COMIDA o CENA", example = "CENA")
        String momento,

        @Schema(description = "ID de la receta cocinada", example = "1")
        Integer idReceta,

        @Schema(description = "Nombre de la receta", example = "Tortilla de espinacas y queso")
        String receta,

        @Schema(description = "Calorías de la receta", example = "600")
        Integer calorias

        

) {
}
