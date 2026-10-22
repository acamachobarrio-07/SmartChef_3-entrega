
package es.safareyes.smartchef.dtos.request;

import es.safareyes.smartchef.entity.ERecetaDificultad;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record RecetasRequest(

        @Positive(message = "El tiempo debe ser mayor que 0")
        Integer tiempoMax,

        ERecetaDificultad dificultad,

        Boolean vegetariano,

        Boolean sinGluten,

        @Min(value = 0, message = "La página no puede ser negativa")
        Integer page,

        @Min(value = 1, message = "El tamaño debe ser al menos 1")
        Integer size

) {
}
