package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.request.IngredienteRequest;
import es.safareyes.smartchef.dtos.response.IngredienteResponse;
import es.safareyes.smartchef.mapper.IngredienteMapper;
import es.safareyes.smartchef.repository.IIngredientesRepository;
import es.safareyes.smartchef.service.IIngredienteService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class IngredienteServiceImpl implements IIngredienteService {

    private IIngredientesRepository ingredientesRepository;
    private IngredienteMapper ingredienteMapper;

    @Override
    public Page<IngredienteResponse> buscarIngredientes(IngredienteRequest filtros) {

        // Establecemos los valores por defecto
        int pagina = filtros.page() == null ? 0 : filtros.page();
        int tamano = filtros.size() == null ? 10 : filtros.size();

        // Ordenamos los ingredientes alfabéticamente
        Pageable pageable = PageRequest.of(
                pagina,
                tamano,
                Sort.by("nombre").ascending()
        );

        // Si no se introduce texto, buscamos todos los ingredientes
        String texto = filtros.texto() == null ? "" : filtros.texto().trim();

        // Consultamos los ingredientes y los convertimos a DTO
        return ingredientesRepository
                .findByNombreContainingIgnoreCase(texto, pageable)
                .map(ingredienteMapper::toDto);
    }
}