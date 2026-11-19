package es.safareyes.smartchef.dtos.response;

import es.safareyes.smartchef.entity.EEstadoLista;

import java.util.List;

public record ListaCompraResponse(
        Integer idUsuario,
        EEstadoLista estado,
        List<ListaCompraIngredienteResponse> ingredientes
) {
}
