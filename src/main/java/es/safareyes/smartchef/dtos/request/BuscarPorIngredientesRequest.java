package es.safareyes.smartchef.dtos.request;

import jakarta.validation.constraints.Positive;

import java.util.List;

public record BuscarPorIngredientesRequest(

        @Positive(message = "El ID del usuario debe ser positivo")
        Integer idUsuario,

        List<@Positive(message = "El ID del ingrediente debe ser positivo") Integer> idsIngredientes


) {
}
