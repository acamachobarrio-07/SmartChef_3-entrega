package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.request.CrearRecetaRequest;
import es.safareyes.smartchef.dtos.request.RecetasRequest;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import org.springframework.data.domain.Page;

public interface IRecetaService {

    // Consultar recetas con filtros
    Page<RecetaResponse> buscarRecetas(RecetasRequest filtros);

    // Crear una receta
    RecetaResponse crearReceta(CrearRecetaRequest dto);

}
