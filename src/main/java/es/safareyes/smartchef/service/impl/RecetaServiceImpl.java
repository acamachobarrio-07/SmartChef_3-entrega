package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.request.BuscarPorIngredientesRequest;
import es.safareyes.smartchef.dtos.request.GuardarRecetaRequest;
import es.safareyes.smartchef.dtos.request.IngredienteRecetaRequest;
import es.safareyes.smartchef.dtos.request.PasoRecetaRequest;
import es.safareyes.smartchef.dtos.request.RecetasRequest;
import es.safareyes.smartchef.dtos.response.BuscarPorIngredientesResponse;
import es.safareyes.smartchef.dtos.response.IngredienteResponse;
import es.safareyes.smartchef.dtos.response.RecetaDetalleResponse;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import es.safareyes.smartchef.entity.Despensa;
import es.safareyes.smartchef.entity.EIngredientesTipoCantidad;
import es.safareyes.smartchef.entity.EPreferenciasTipo;
import es.safareyes.smartchef.entity.Ingredientes;
import es.safareyes.smartchef.entity.IngredientesReceta;
import es.safareyes.smartchef.entity.Paso;
import es.safareyes.smartchef.entity.Receta;
import es.safareyes.smartchef.exception.ElementoDuplicado;
import es.safareyes.smartchef.exception.RelacionNoEncontrada;
import es.safareyes.smartchef.exception.ReglaNegocioException;
import es.safareyes.smartchef.mapper.IIngredienteMapper;
import es.safareyes.smartchef.mapper.IRecetaMapper;
import es.safareyes.smartchef.repository.IDespensaRepository;
import es.safareyes.smartchef.repository.IIngredientesRecetaRepository;
import es.safareyes.smartchef.repository.IIngredientesRepository;
import es.safareyes.smartchef.repository.IPasoRepository;
import es.safareyes.smartchef.repository.IRecetaRepository;
import es.safareyes.smartchef.repository.IUsuarioRepository;
import es.safareyes.smartchef.service.IRecetaService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RecetaServiceImpl implements IRecetaService {

    private IRecetaRepository recetaRepository;
    private IRecetaMapper recetaMapper;
    private IIngredientesRepository ingredientesRepository;
    private IIngredientesRecetaRepository ingredientesRecetaRepository;
    private IPasoRepository pasoRepository;
    private IUsuarioRepository usuarioRepository;
    private IDespensaRepository despensaRepository;
    private IIngredienteMapper ingredienteMapper;

    // GET - Buscar recetas con filtros
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

    // POST - Crear una receta
    @Override
    @Transactional
    public RecetaResponse crearReceta(GuardarRecetaRequest json) {

        // 1. Comprobar que el nombre no esté repetido
        if (recetaRepository.existsByNombreIgnoreCase(json.nombre())) {
            throw new ElementoDuplicado(
                    "Ya existe una receta con el nombre: " + json.nombre()
            );
        }

        // 2. Comprobar que los pasos estén numerados sin huecos
        List<Integer> numeros = json.pasos().stream()
                .map(PasoRecetaRequest::numero)
                .sorted()
                .toList();

        for (int i = 0; i < numeros.size(); i++) {
            if (numeros.get(i) != i + 1) {
                throw new ReglaNegocioException(
                        "Los pasos deben estar numerados desde 1 y sin huecos"
                );
            }
        }

        // 3. Buscar ingredientes y calcular calorías
        Set<Integer> idsIngredientes = new HashSet<>();
        List<IngredientesReceta> relaciones = new ArrayList<>();
        BigDecimal caloriasTotales = BigDecimal.ZERO;

        for (IngredienteRecetaRequest item : json.ingredientes()) {

            // Evitar ingredientes repetidos
            if (!idsIngredientes.add(item.idIngrediente())) {
                throw new ReglaNegocioException(
                        "No se puede repetir un ingrediente en la receta"
                );
            }

            // Comprobar que el ingrediente existe
            Ingredientes ingrediente = ingredientesRepository
                    .findById(item.idIngrediente())
                    .orElseThrow(() -> new RelacionNoEncontrada(
                            "No existe el ingrediente con ID: "
                                    + item.idIngrediente()
                    ));

            // Comprobar los datos del ingrediente
            if (ingrediente.getCalorias() == null
                    || ingrediente.getEIngredientesTipoCantidad() == null) {

                throw new ReglaNegocioException(
                        "El ingrediente no tiene datos suficientes: "
                                + ingrediente.getNombre()
                );
            }

            // Calcular calorías del ingrediente
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

            // Crear relación ingrediente-receta
            IngredientesReceta relacion = new IngredientesReceta();
            relacion.setIngrediente(ingrediente);
            relacion.setCantidad(item.cantidad());

            relaciones.add(relacion);
        }

        // 4. Convertir DTO a entidad mediante MapStruct
        Receta receta = recetaMapper.toEntity(json);

        // Calcular calorías por ración
        int caloriasPorRacion = caloriasTotales
                .divide(
                        BigDecimal.valueOf(json.raciones()),
                        0,
                        RoundingMode.HALF_UP
                )
                .intValueExact();

        receta.setCalorias(caloriasPorRacion);

        // Guardar receta
        Receta recetaGuardada = recetaRepository.save(receta);

        // 5. Guardar ingredientes y cantidades
        for (IngredientesReceta relacion : relaciones) {
            relacion.setReceta(recetaGuardada);
        }

        ingredientesRecetaRepository.saveAll(relaciones);

        // 6. Guardar los pasos
        List<Paso> pasos = new ArrayList<>();

        for (PasoRecetaRequest item : json.pasos()) {

            Paso paso = new Paso();
            paso.setNumero(item.numero());
            paso.setDescripcion(item.descripcion());
            paso.setReceta(recetaGuardada);

            pasos.add(paso);
        }

        pasoRepository.saveAll(pasos);

        // 7. Convertir entidad a DTO de respuesta
        return recetaMapper.aResponse(recetaGuardada);
    }

    // POST - Buscar recetas por ingredientes (RN-04)
    @Override
    public List<BuscarPorIngredientesResponse> buscarPorIngredientes(
            BuscarPorIngredientesRequest datos) {

        // 1. Comprobar cómo queremos buscar
        if (datos.idUsuario() != null && datos.idsIngredientes() != null) {
            throw new ReglaNegocioException(
                    "Debes indicar una lista de ingredientes o un usuario, no ambos"
            );
        }

        if (datos.idUsuario() == null &&
                (datos.idsIngredientes() == null || datos.idsIngredientes().isEmpty())) {
            throw new ReglaNegocioException(
                    "Debes indicar ingredientes o un usuario"
            );
        }

        // 2. Obtener los ingredientes disponibles
        Set<Integer> disponibles = new HashSet<>();

        if (datos.idUsuario() != null) {

            if (!usuarioRepository.existsById(datos.idUsuario())) {
                throw new RelacionNoEncontrada(
                        "No existe el usuario con ID: " + datos.idUsuario()
                );
            }

            // Solo ingredientes con cantidad positiva y no caducados
            List<Despensa> despensa = despensaRepository.buscarIngredientesDisponibles(
                    datos.idUsuario(),
                    LocalDate.now()
            );

            for (Despensa item : despensa) {
                disponibles.add(item.getIngrediente().getId());
            }

        } else {
            disponibles.addAll(datos.idsIngredientes());
        }

        // 3. Obtener las recetas y sus ingredientes
        List<Receta> recetas = recetaRepository.buscarRecetasConIngredientes();

        List<BuscarPorIngredientesResponse> resultados = new ArrayList<>();

        for (Receta receta : recetas) {

            List<IngredientesReceta> ingredientes = receta.getIngredientesReceta();

            int total = ingredientes.size();

            // Evitar recetas sin ingredientes
            if (total == 0) {
                continue;
            }

            int coincidentes = 0;
            List<IngredienteResponse> faltantes = new ArrayList<>();

            // 4. Comprobar qué ingredientes tenemos
            for (IngredientesReceta ir : ingredientes) {

                Ingredientes ingrediente = ir.getIngrediente();

                if (disponibles.contains(ingrediente.getId())) {
                    coincidentes++;
                } else {
                    faltantes.add(ingredienteMapper.toDto(ingrediente));
                }
            }

            // 5. RN-04: necesitamos MÁS del 50 %
            if (coincidentes * 2 <= total) {
                continue;
            }

            // 6. Calcular porcentaje de coincidencia
            BigDecimal porcentaje = BigDecimal.valueOf(coincidentes)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                            BigDecimal.valueOf(total),
                            2,
                            RoundingMode.HALF_UP
                    );

            // 7. Construir la respuesta
            resultados.add(new BuscarPorIngredientesResponse(
                    recetaMapper.aResponse(receta),
                    coincidentes,
                    total,
                    porcentaje,
                    faltantes
            ));
        }

        // 8. Ordenar de mayor a menor coincidencia
        resultados.sort(
                Comparator.comparing(
                        BuscarPorIngredientesResponse::porcentajeCoincidencia
                ).reversed()
        );

        return resultados;
    }

    // GET - Consultar receta por ID con sus datos calculados
    @Override
    public RecetaDetalleResponse obtenerRecetaPorId(Integer id) {

        // 1. Buscar la receta
        Receta receta = recetaRepository.findById(id)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe una receta con ID: " + id
                ));

        // 2. Obtener sus ingredientes
        List<IngredientesReceta> ingredientes = receta.getIngredientesReceta();

        // 3. Calcular el coste y las preferencias
        BigDecimal costeTotal = BigDecimal.ZERO;
        boolean vegetariano = true;
        boolean sinGluten = true;

        for (IngredientesReceta ir : ingredientes) {

            Ingredientes ingrediente = ir.getIngrediente();

            if (ir.getCantidad() == null
                    || ingrediente.getPrecio() == null
                    || ingrediente.getEIngredientesTipoCantidad() == null) {
                throw new ReglaNegocioException(
                        "Faltan datos para calcular el coste de la receta"
                );
            }

            BigDecimal coste = ir.getCantidad()
                    .multiply(ingrediente.getPrecio());

            if (ingrediente.getEIngredientesTipoCantidad()
                    != EIngredientesTipoCantidad.UNIDADES) {

                coste = coste.divide(
                        BigDecimal.valueOf(100),
                        4,
                        RoundingMode.HALF_UP
                );
            }

            costeTotal = costeTotal.add(coste);

            boolean aptoVegetariano = ingrediente.getAlergias()
                    .stream()
                    .anyMatch(p -> p.getEPreferenciasTipo()
                            == EPreferenciasTipo.VEGETARIANO);

            boolean aptoSinGluten = ingrediente.getAlergias()
                    .stream()
                    .anyMatch(p -> p.getEPreferenciasTipo()
                            == EPreferenciasTipo.SINGLUTEN);

            vegetariano = vegetariano && aptoVegetariano;
            sinGluten = sinGluten && aptoSinGluten;
        }

        // 4. Calcular el coste por ración
        if (receta.getRaciones() == null || receta.getRaciones() <= 0) {
            throw new ReglaNegocioException(
                    "La receta debe tener al menos una ración"
            );
        }

        BigDecimal costePorRacion = costeTotal.divide(
                BigDecimal.valueOf(receta.getRaciones()),
                2,
                RoundingMode.HALF_UP
        );

        boolean rapido = receta.getTiempo() != null
                && receta.getTiempo() <= 30;

        boolean economico = costeTotal.compareTo(
                BigDecimal.valueOf(receta.getRaciones())
                        .multiply(BigDecimal.valueOf(2.50))
        ) <= 0;

        // 5. Ordenar los pasos
        List<Paso> pasosOrdenados = receta.getPasos()
                .stream()
                .sorted(Comparator.comparing(Paso::getNumero))
                .toList();

        // 6. Convertir los datos mediante MapStruct
        return new RecetaDetalleResponse(
                recetaMapper.aResponse(receta),
                costePorRacion,
                vegetariano,
                sinGluten,
                rapido,
                economico,
                recetaMapper.toIngredientesDto(ingredientes),
                recetaMapper.toPasosDto(pasosOrdenados)
        );
    }

    //PUT - Modificar receta

    @Override
    @Transactional
    public RecetaResponse modificarReceta(Integer id, GuardarRecetaRequest dto) {

        // 1. Buscar la receta
        Receta receta = recetaRepository.findById(id)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe una receta con ID: " + id
                ));

        // 2. Comprobar que el nombre no pertenezca a otra receta
        if (!receta.getNombre().equalsIgnoreCase(dto.nombre())
                && recetaRepository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ElementoDuplicado(
                    "Ya existe una receta con el nombre: " + dto.nombre()
            );
        }

        // 3. Comprobar que los pasos estén numerados sin huecos
        List<Integer> numeros = dto.pasos().stream()
                .map(PasoRecetaRequest::numero)
                .sorted()
                .toList();

        for (int i = 0; i < numeros.size(); i++) {
            if (numeros.get(i) != i + 1) {
                throw new ReglaNegocioException(
                        "Los pasos deben estar numerados desde 1 y sin huecos"
                );
            }
        }

        // 4. Preparar ingredientes y recalcular calorías
        Set<Integer> idsIngredientes = new HashSet<>();
        List<IngredientesReceta> relaciones = new ArrayList<>();
        BigDecimal caloriasTotales = BigDecimal.ZERO;

        for (IngredienteRecetaRequest item : dto.ingredientes()) {

            if (!idsIngredientes.add(item.idIngrediente())) {
                throw new ReglaNegocioException(
                        "No se puede repetir un ingrediente en la receta"
                );
            }

            Ingredientes ingrediente = ingredientesRepository
                    .findById(item.idIngrediente())
                    .orElseThrow(() -> new RelacionNoEncontrada(
                            "No existe el ingrediente con ID: " + item.idIngrediente()
                    ));

            if (ingrediente.getCalorias() == null
                    || ingrediente.getEIngredientesTipoCantidad() == null) {
                throw new ReglaNegocioException(
                        "El ingrediente no tiene datos suficientes: "
                                + ingrediente.getNombre()
                );
            }

            BigDecimal calorias = BigDecimal
                    .valueOf(ingrediente.getCalorias())
                    .multiply(item.cantidad());

            if (ingrediente.getEIngredientesTipoCantidad()
                    != EIngredientesTipoCantidad.UNIDADES) {
                calorias = calorias.divide(
                        BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP
                );
            }

            caloriasTotales = caloriasTotales.add(calorias);

            IngredientesReceta relacion = new IngredientesReceta();
            relacion.setIngrediente(ingrediente);
            relacion.setCantidad(item.cantidad());
            relacion.setReceta(receta);
            relaciones.add(relacion);
        }

        // 5. Modificar los campos simples mediante MapStruct
        recetaMapper.actualizarReceta(dto, receta);

        // 6. Calcular calorías por ración
        int caloriasPorRacion = caloriasTotales.divide(
                BigDecimal.valueOf(dto.raciones()), 0, RoundingMode.HALF_UP
        ).intValueExact();
        receta.setCalorias(caloriasPorRacion);

        // 7. Guardar referencia a los registros hijos antiguos
        List<IngredientesReceta> ingredientesAntiguos =
                new ArrayList<>(receta.getIngredientesReceta());
        List<Paso> pasosAntiguos = new ArrayList<>(receta.getPasos());

        // 8. Quitar las relaciones antiguas de las colecciones gestionadas
        receta.getIngredientesReceta().clear();
        receta.getPasos().clear();

        // 9. Eliminarlas explícitamente (no utilizamos orphanRemoval)
        ingredientesRecetaRepository.deleteAll(ingredientesAntiguos);
        pasoRepository.deleteAll(pasosAntiguos);
        ingredientesRecetaRepository.flush();
        pasoRepository.flush();

        // 10. Añadir los hijos nuevos por ambos lados de la relación
        receta.getIngredientesReceta().addAll(relaciones);
        for (PasoRecetaRequest item : dto.pasos()) {
            Paso paso = new Paso();
            paso.setNumero(item.numero());
            paso.setDescripcion(item.descripcion());
            paso.setReceta(receta);
            receta.getPasos().add(paso);
        }

        // 11. Guardar la receta; cascade = ALL persiste los nuevos hijos
        Receta recetaGuardada = recetaRepository.save(receta);
        return recetaMapper.aResponse(recetaGuardada);
    }


    // PATCH - Retirar una receta (baja lógica)
    @Override
    @Transactional
    public void retirarReceta(Integer id) {

        // 1. Buscar la receta por su ID
        Receta receta = recetaRepository.findById(id)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe una receta con ID: " + id
                ));

        // 2. Comprobar si ya está retirada
        if (Boolean.TRUE.equals(receta.getRetirada())) {
            throw new ReglaNegocioException(
                    "La receta ya está retirada"
            );
        }

        // 3. Cambiar el estado de la receta
        receta.setRetirada(true);

        // 4. Guardar los cambios
        recetaRepository.save(receta);
    }

}
