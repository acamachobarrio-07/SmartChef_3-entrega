package es.safareyes.smartchef.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PasoRecetaRequest(

        @NotNull(message = "El número del paso es obligatorio")
        @Min(value = 1, message = "El número del paso debe ser mayor que 0")
        Integer numero,

        @NotBlank(message = "La descripción del paso es obligatoria")
        String descripcion

) {
}