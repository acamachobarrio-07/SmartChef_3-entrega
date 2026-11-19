
package es.safareyes.smartchef.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DesdeRecetaRequest(

        @NotNull(message = "El ID de la receta es obligatorio")
        @Min(value = 1, message = "El ID de la receta debe ser positivo")
        Integer idReceta,

        @NotNull(message = "El número de raciones es obligatorio")
        @Positive(message = "El número de raciones debe ser positivo")
        @Max(value = 12, message = "No se pueden solicitar más de 12 raciones")
        Integer raciones

) {
}
