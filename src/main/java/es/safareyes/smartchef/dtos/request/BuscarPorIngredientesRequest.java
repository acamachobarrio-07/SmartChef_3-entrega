package es.safareyes.smartchef.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record BuscarPorIngredientesRequest(

        @Schema(description = "ID del usuario cuya despensa se usará", example = "2")
        @Positive(message = "El ID del usuario debe ser positivo")
        Integer idUsuario,

        @Schema(description = "IDs de los ingredientes disponibles", example = "[1, 2, 3]")
        List<@Positive(message = "El ID del ingrediente debe ser positivo") Integer> idsIngredientes


) {
}
