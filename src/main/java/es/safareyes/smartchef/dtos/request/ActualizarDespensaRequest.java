package es.safareyes.smartchef.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActualizarDespensaRequest(

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.0", message = "La cantidad no puede ser negativa")
        BigDecimal cantidad,

        // Opcional: hay ingredientes sin fecha de caducidad (sal, aceite...)
        LocalDate caducidad
) {
}
