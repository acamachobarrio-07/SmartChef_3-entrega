
package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.request.CrearRecetaRequest;
import es.safareyes.smartchef.dtos.request.RecetasRequest;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import org.springframework.data.domain.Page;

public interface RecetaService {

    //buscar recetas por filtros
    Page<RecetaResponse> buscarRecetas(RecetasRequest filtros);

    //crear receta
    RecetaResponse crearReceta(CrearRecetaRequest request);

}
