package es.safareyes.smartchef.dtos.response;

import es.safareyes.smartchef.entity.ERecetaDificultad;

public record RecetaColeccionResponse(
        Integer id,
        String nombre,
        Integer tiempo,
        ERecetaDificultad dificultad,
        Boolean retirada
) {
}
