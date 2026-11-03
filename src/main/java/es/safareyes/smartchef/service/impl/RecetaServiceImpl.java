package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.request.CrearRecetaRequest;
import es.safareyes.smartchef.dtos.request.IngredienteRecetaRequest;
import es.safareyes.smartchef.dtos.request.PasoRecetaRequest;
import es.safareyes.smartchef.dtos.request.RecetasRequest;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import es.safareyes.smartchef.entity.*;
import es.safareyes.smartchef.mapper.RecetaMapper;
import es.safareyes.smartchef.repository.IIngredientesRecetaRepository;
import es.safareyes.smartchef.repository.IIngredientesRepository;
import es.safareyes.smartchef.repository.IPasoRepository;
import es.safareyes.smartchef.repository.IRecetaRepository;
import es.safareyes.smartchef.service.RecetaService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecetaServiceImpl implements RecetaService {

    private  IRecetaRepository recetaRepository;
    private  RecetaMapper recetaMapper;
    private  IIngredientesRepository ingredientesRepository;
    private  IIngredientesRecetaRepository ingredientesRecetaRepository;
    private  IPasoRepository pasoRepository;


    @Override
    public Page<RecetaResponse> buscarRecetas(RecetasRequest filtros) {

        int pagina = filtros.page() == null ? 0 : filtros.page();
        int tamano = filtros.size() == null ? 10 : filtros.size();

        Pageable pageable = PageRequest.of(pagina, tamano);

        Integer tiempoMax = filtros.tiempoMax();

        if (Boolean.TRUE.equals(filtros.rapido())) {
            tiempoMax = tiempoMax == null
                    ? 30
                    : Math.min(tiempoMax, 30);
        }

        return recetaRepository.buscarConFiltros(
                tiempoMax,
                filtros.dificultad(),
                filtros.vegetariano(),
                filtros.sinGluten(),
                filtros.economico(),
                pageable
        ).map(recetaMapper::aResponseDesdeFiltro);
    }


    @Override
    @Transactional
    public RecetaResponse crearReceta(CrearRecetaRequest json) {

        // 1. Comprobar que los pasos estén numerados sin huecos
        List<Integer> numeros = json.pasos().stream()
                .map(PasoRecetaRequest::numero)
                .sorted()
                .toList();

        for (int i = 0; i < numeros.size(); i++) {
            if (numeros.get(i) != i + 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Los pasos deben estar numerados desde 1 y sin huecos"
                );
            }
        }

        // 2. Buscar los ingredientes y calcular las calorías
        Set<Integer> idsIngredientes = new HashSet<>();
        List<IngredientesReceta> relaciones = new ArrayList<>();
        BigDecimal caloriasTotales = BigDecimal.ZERO;

        for (IngredienteRecetaRequest item : json.ingredientes()) {

            if (!idsIngredientes.add(item.idIngrediente())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No se puede repetir un ingrediente"
                );
            }

            Ingredientes ingrediente = ingredientesRepository
                    .findById(item.idIngrediente())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No existe el ingrediente con ID " + item.idIngrediente()
                    ));

            if (ingrediente.getCalorias() == null
                    || ingrediente.getEIngredientesTipoCantidad() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El ingrediente no tiene datos suficientes: "
                                + ingrediente.getNombre()
                );
            }

            BigDecimal calorias = BigDecimal
                    .valueOf(ingrediente.getCalorias())
                    .multiply(item.cantidad());

            // Gramos y mililitros: calorías por cada 100
            if (ingrediente.getEIngredientesTipoCantidad()
                    != EIngredientesTipoCantidad.UNIDADES) {

                calorias = calorias.divide(
                        BigDecimal.valueOf(100),
                        4,
                        RoundingMode.HALF_UP
                );
            }

            caloriasTotales = caloriasTotales.add(calorias);

            IngredientesReceta relacion = new IngredientesReceta();
            relacion.setIngrediente(ingrediente);
            relacion.setCantidad(item.cantidad());

            relaciones.add(relacion);
        }

        // 3. Crear y guardar la receta
        Receta receta = new Receta();

        receta.setNombre(json.nombre());
        receta.setDescripcion(json.descripcion());
        receta.setRaciones(json.raciones());
        receta.setTiempo(json.tiempo());
        receta.setERecetaDificultad(json.dificultad());

        int caloriasPorRacion = caloriasTotales
                .divide(
                        BigDecimal.valueOf(json.raciones()),
                        0,
                        RoundingMode.HALF_UP
                )
                .intValueExact();

        receta.setCalorias(caloriasPorRacion);

        Receta recetaGuardada = recetaRepository.save(receta);

        // 4. Guardar ingredientes y cantidades
        for (IngredientesReceta relacion : relaciones) {
            relacion.setReceta(recetaGuardada);
        }

        ingredientesRecetaRepository.saveAll(relaciones);

        // 5. Guardar los pasos de preparación
        List<Paso> pasos = new ArrayList<>();

        for (PasoRecetaRequest item : json.pasos()) {

            Paso paso = new Paso();
            paso.setNumero(item.numero());
            paso.setDescripcion(item.descripcion());
            paso.setReceta(recetaGuardada);

            pasos.add(paso);
        }

        pasoRepository.saveAll(pasos);

        // 6. Devolver los datos de la receta creada
        return new RecetaResponse(
                recetaGuardada.getId(),
                recetaGuardada.getNombre(),
                recetaGuardada.getDescripcion(),
                recetaGuardada.getTiempo(),
                recetaGuardada.getRaciones(),
                recetaGuardada.getCalorias(),
                recetaGuardada.getERecetaDificultad()
        );
    }


}
