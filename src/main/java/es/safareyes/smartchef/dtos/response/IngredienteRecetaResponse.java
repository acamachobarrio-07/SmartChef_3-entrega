package es.safareyes.smartchef.dtos.response;

import es.safareyes.smartchef.entity.EIngredientesTipoCantidad;
import java.math.BigDecimal;

public record IngredienteRecetaResponse(

        Integer idIngrediente,
        String nombre,
        BigDecimal cantidad,
        EIngredientesTipoCantidad tipoCantidad

) {
}