package es.safareyes.smartchef.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record IngredienteRequest(

        @Size(max = 100)
        String texto,

        @Min(0)
        Integer page,

        @Min(1)
        @Max(100)
        Integer size
) {
}
