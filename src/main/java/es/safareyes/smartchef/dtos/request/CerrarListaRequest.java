package es.safareyes.smartchef.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CerrarListaRequest(

        @Schema(description = "IDs de las filas de la lista que se han comprado (puede ir vacía)", example = "[4, 7]")
        @NotNull(message = "Indica los Id comprados (puede ser una lista vacia")
        List<@Positive (message = "El ID debe ser positivo") Integer> idsComprados

) {
}
