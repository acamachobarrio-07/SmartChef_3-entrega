package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.request.DesdeRecetaRequest;
import es.safareyes.smartchef.dtos.response.ListaCompraResponse;

public interface IListaCompraService {

    ListaCompraResponse generarDesdeReceta(
            Integer idUsuario,
            DesdeRecetaRequest request
    );

}
