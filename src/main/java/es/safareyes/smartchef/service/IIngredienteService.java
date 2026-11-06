package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.request.IngredienteRequest;
import es.safareyes.smartchef.dtos.response.IngredienteResponse;
import org.springframework.data.domain.Page;

public interface IIngredienteService {

    // Obtener el catálogo de ingredientes con filtros
    Page<IngredienteResponse> buscarIngredientes(IngredienteRequest filtros);

}
