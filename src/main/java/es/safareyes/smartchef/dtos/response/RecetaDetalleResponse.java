package es.safareyes.smartchef.dtos.response;

import java.math.BigDecimal;
import java.util.List;

public record RecetaDetalleResponse(

        //reutilizamos los atributos de RecetaResponse para
        // no duplicar código imnecesario y añadimos los
        // que necesitamos para filtrar por id y
        // tener todos los datos de esa receta
        RecetaResponse receta,
        BigDecimal costePorRacion,
        Boolean vegetariano,
        Boolean sinGluten,
        Boolean rapido,
        Boolean economico,

        List<IngredienteRecetaResponse> ingredientes,
        List<PasoRecetaResponse> pasos

) {
}

