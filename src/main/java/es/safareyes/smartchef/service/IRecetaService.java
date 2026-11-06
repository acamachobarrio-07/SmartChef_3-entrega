package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.request.BuscarPorIngredientesRequest;
import es.safareyes.smartchef.dtos.request.CrearRecetaRequest;
import es.safareyes.smartchef.dtos.request.RecetasRequest;
import es.safareyes.smartchef.dtos.response.BuscarPorIngredientesResponse;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IRecetaService {

    // Consultar recetas con filtros
    Page<RecetaResponse> buscarRecetas(RecetasRequest filtros);

    // Crear una receta
    RecetaResponse crearReceta(CrearRecetaRequest dto);

    // Buscar recetas según los ingredientes disponibles
    List<BuscarPorIngredientesResponse> buscarPorIngredientes(
            BuscarPorIngredientesRequest datos
    );

}
