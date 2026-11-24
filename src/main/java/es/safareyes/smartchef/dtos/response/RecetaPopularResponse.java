package es.safareyes.smartchef.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RecetaPopularResponse(

        @Schema(description = "ID de la receta", example = "1")
        Integer id,

        @Schema(description = "Nombre de la receta", example = "Tortilla de espinacas y queso")
        String nombre,

        @Schema(description = "Si la receta está retirada", example = "false")
        Boolean retirada,

        @Schema(description = "Veces cocinada o veces guardada", example = "12")
        Long total

) {
}
