package es.safareyes.smartchef.dtos.response;

import es.safareyes.smartchef.entity.ERecetaDificultad;

public record RecetaResponse(
        Integer id,
        String nombre,
        String descripcion,
        Integer tiempo,
        Integer raciones,
        Integer calorias,
        ERecetaDificultad dificultad
) {
}
