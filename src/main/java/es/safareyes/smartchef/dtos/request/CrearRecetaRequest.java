
package es.safareyes.smartchef.dtos.request;

import es.safareyes.smartchef.entity.ERecetaDificultad;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CrearRecetaRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        String descripcion,

        @NotNull(message = "Las raciones son obligatorias")
        @Min(value = 1, message = "Debe tener al menos 1 ración")
        @Max(value = 12, message = "No puede superar las 12 raciones")
        Integer raciones,

        @NotNull(message = "El tiempo es obligatorio")
        @Positive(message = "El tiempo debe ser mayor que 0")
        Integer tiempo,

        @NotNull(message = "La dificultad es obligatoria")
        ERecetaDificultad dificultad,

        @NotEmpty(message = "Debe incluir al menos un ingrediente")
        @Size(max = 30, message = "No puede superar los 30 ingredientes")
        List<@Valid IngredienteRecetaRequest> ingredientes,

        @NotEmpty(message = "Debe incluir al menos un paso")
        List<@Valid PasoRecetaRequest> pasos

) {
}
