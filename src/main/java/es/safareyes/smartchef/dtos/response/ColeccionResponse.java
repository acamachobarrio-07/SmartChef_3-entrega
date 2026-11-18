package es.safareyes.smartchef.dtos.response;

import java.util.List;

public record ColeccionResponse(

        Integer id,
        String nombre,
        List<RecetaColeccionResponse> recetas

) {
}
