package es.safareyes.smartchef.dtos.response;

import es.safareyes.smartchef.entity.EIngredientesTipoCantidad;

import java.math.BigDecimal;

public record IngredienteResponse(

        Integer id,
        String nombre,
        EIngredientesTipoCantidad tipoCantidad,
        Integer calorias,
        BigDecimal precio
) {
}
