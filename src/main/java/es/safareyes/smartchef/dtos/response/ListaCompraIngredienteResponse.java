package es.safareyes.smartchef.dtos.response;

import es.safareyes.smartchef.entity.EIngredientesTipoCantidad;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ListaCompraIngredienteResponse(
        Integer id,
        Integer idIngrediente,
        String nombre,
        EIngredientesTipoCantidad tipoCantidad,
        BigDecimal cantidad,
        LocalDateTime fecha,
        Boolean comprado
) {
}
