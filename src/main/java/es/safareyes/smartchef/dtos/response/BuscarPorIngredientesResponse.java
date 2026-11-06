package es.safareyes.smartchef.dtos.response;

import java.math.BigDecimal;
import java.util.List;

public record BuscarPorIngredientesResponse(

        RecetaResponse receta,
        Integer ingredientesCoincidentes, //cuantos ingredientes tenemos
        Integer ingredientesTotales, //cuantos necesita la receta
        BigDecimal porcentajeCoincidencia, //porcentaje de ingredientes disponibles
        List<IngredienteResponse> ingredientesFaltantes //lista de ingredientes que nos falta

) {
}
