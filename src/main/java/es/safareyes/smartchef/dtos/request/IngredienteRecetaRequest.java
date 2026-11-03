
package es.safareyes.smartchef.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record IngredienteRecetaRequest(

        @NotNull(message = "El ingrediente es obligatorio")
        Integer idIngrediente,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "La cantidad debe ser mayor que 0"
        )
        BigDecimal cantidad

) {
}
