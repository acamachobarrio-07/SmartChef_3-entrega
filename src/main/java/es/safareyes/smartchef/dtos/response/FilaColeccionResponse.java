package es.safareyes.smartchef.dtos.response;

import es.safareyes.smartchef.entity.ERecetaDificultad;

public record FilaColeccionResponse(
        Integer idColeccion,
        String coleccion,
        Integer idReceta,
        String receta,
        Integer tiempo,
        ERecetaDificultad dificultad,
        Boolean retirada
) {
}
