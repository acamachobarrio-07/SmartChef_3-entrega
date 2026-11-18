package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.request.ActualizarDespensaRequest;
import es.safareyes.smartchef.dtos.request.DespensaRequest;
import es.safareyes.smartchef.dtos.response.DespensaResponse;

import java.util.List;

public interface IDespensaService {

    // Consultar la despensa de un usuario
    List<DespensaResponse> obtenerDespensa(DespensaRequest datos);

    // Añadir o actualizar un ingrediente de la despensa (cantidad 0 lo elimina)
    DespensaResponse guardarEnDespensa(
            Integer idUsuario,
            Integer idIngrediente,
            ActualizarDespensaRequest datos
    );

}