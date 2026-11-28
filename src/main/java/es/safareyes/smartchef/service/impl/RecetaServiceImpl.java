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
import es.safareyes.smartchef.dtos.ICoincidenciaDespensaProyeccion;
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
import es.safareyes.smartchef.entity.Preferencias;
import es.safareyes.smartchef.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
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

    // GET - Buscar recetas con filtros (RN-03 y RN-09)
    @Override
    public Page<RecetaResponse> buscarRecetas(RecetasRequest filtros) {

        int pagina = filtros.page() == null ? 0 : filtros.page();
        int tamano = filtros.size() == null ? 10 : filtros.size();
        Pageable pageable = PageRequest.of(pagina, tamano);

        // filtros que vienen en la petición (pueden ser null)
        Boolean vegetariano = filtros.vegetariano();
        Boolean sinGluten = filtros.sinGluten();
        Boolean rapido = filtros.rapido();
        Boolean economico = filtros.economico();

        // RN-03: si se indica usuario, sus preferencias se aplican por defecto
        //    SOLO en los filtros que la petición no indica
        if (filtros.idUsuario() != null) {

            Usuario usuario = usuarioRepository.findById(filtros.idUsuario())
                    .orElseThrow(() -> new RelacionNoEncontrada(
                            "No existe el usuario con ID: " + filtros.idUsuario()
                    ));

            // pasamos las preferencias del perfil a un Set de enums
            Set<EPreferenciasTipo> perfil = new HashSet<>();
            for (Preferencias p : usuario.getPreferencias()) {
                perfil.add(p.getEPreferenciasTipo());
            }

            if (vegetariano == null && perfil.contains(EPreferenciasTipo.VEGETARIANO)) {
                vegetariano = true;
            }
            if (sinGluten == null && perfil.contains(EPreferenciasTipo.SINGLUTEN)) {
                sinGluten = true;
            }
            if (rapido == null && perfil.contains(EPreferenciasTipo.RAPIDO)) {
                rapido = true;
            }
            if (economico == null && perfil.contains(EPreferenciasTipo.ECONOMICO)) {
                economico = true;
            }
        }

        // rápida = 30 minutos o menos (RN-02)
        Integer tiempoMax = filtros.tiempoMax();
        if (Boolean.TRUE.equals(rapido)) {
            tiempoMax = tiempoMax == null
                    ? 30
                    : Math.min(tiempoMax, 30);
        }

        // consultar (la consulta ya excluye las retiradas, RN-09)
        return recetaRepository.buscarConFiltros(
                tiempoMax,
                filtros.dificultad(),
                vegetariano,
                sinGluten,
                economico,
                pageable
        );
    }

    // POST - Crear una receta
    @Override
    @Transactional
    public RecetaResponse crearReceta(GuardarRecetaRequest json) {

        // comprobar que el nombre no esté repetido
        if (recetaRepository.existsByNombreIgnoreCase(json.nombre())) {
            throw new ElementoDuplicado(
                    "Ya existe una receta con el nombre: " + json.nombre()
            );
        }

        // comprobar que los pasos estén numerados sin huecos
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

        // buscar ingredientes y calcular calorías
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

        // convertir DTO a entidad mediante MapStruct
        Receta receta = recetaMapper.toEntity(json);

        // calcular calorías por ración
        int caloriasPorRacion = caloriasTotales
                .divide(
                        BigDecimal.valueOf(json.raciones()),
                        0,
                        RoundingMode.HALF_UP
                )
                .intValueExact();

        receta.setCalorias(caloriasPorRacion);

        // guardar receta
        Receta recetaGuardada = recetaRepository.save(receta);

        // guardar ingredientes y cantidades
        for (IngredientesReceta relacion : relaciones) {
            relacion.setReceta(recetaGuardada);
        }

        ingredientesRecetaRepository.saveAll(relaciones);

        // guardar los pasos
        List<Paso> pasos = new ArrayList<>();

        for (PasoRecetaRequest item : json.pasos()) {

            Paso paso = new Paso();
            paso.setNumero(item.numero());
            paso.setDescripcion(item.descripcion());
            paso.setReceta(recetaGuardada);

            pasos.add(paso);
        }

        pasoRepository.saveAll(pasos);

        log.info("Receta creada: id={} nombre='{}'", recetaGuardada.getId(), recetaGuardada.getNombre());

        // convertir entidad a DTO de respuesta
        return recetaMapper.aResponse(recetaGuardada);
    }

    // POST - Buscar recetas por ingredientes (RN-04)
    @Override
    public List<BuscarPorIngredientesResponse> buscarPorIngredientes(
            BuscarPorIngredientesRequest datos) {

        // comprobar cómo queremos buscar
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

        // por usuario: el cálculo lo hace PostgreSQL (consulta nativa Q3)
        if (datos.idUsuario() != null) {
            return buscarPorDespensa(datos.idUsuario());
        }

        // por lista de ingredientes: lo calculamos en Java
        Set<Integer> disponibles = new HashSet<>(datos.idsIngredientes());

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

            // Comprobar qué ingredientes tenemos
            for (IngredientesReceta ir : ingredientes) {
                Ingredientes ingrediente = ir.getIngrediente();
                if (disponibles.contains(ingrediente.getId())) {
                    coincidentes++;
                } else {
                    faltantes.add(ingredienteMapper.toDto(ingrediente));
                }
            }

            // RN-04: necesitamos MÁS del 50 %
            if (coincidentes * 2 <= total) {
                continue;
            }

            BigDecimal porcentaje = BigDecimal.valueOf(coincidentes)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

            resultados.add(new BuscarPorIngredientesResponse(
                    recetaMapper.aResponse(receta),
                    coincidentes,
                    total,
                    porcentaje,
                    faltantes
            ));
        }

        // Ordenar de mayor a menor coincidencia
        resultados.sort(
                Comparator.comparing(
                        BuscarPorIngredientesResponse::porcentajeCoincidencia
                ).reversed()
        );

        return resultados;
    }

    // Búsqueda por la despensa del usuario (RN-04).
    private List<BuscarPorIngredientesResponse> buscarPorDespensa(Integer idUsuario) {

        // comprobar que el usuario existe
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new RelacionNoEncontrada(
                    "No existe el usuario con ID: " + idUsuario
            );
        }

        // recetas con más del 50 %, ya ordenadas (Q3: GROUP BY + HAVING)
        List<ICoincidenciaDespensaProyeccion> coincidencias =
                recetaRepository.buscarCoincidenciasDespensa(idUsuario);

        List<BuscarPorIngredientesResponse> resultados = new ArrayList<>();

        if (coincidencias.isEmpty()) {
            return resultados;
        }

        // ingredientes disponibles (cantidad > 0 y no caducados) para saber los que faltan
        Set<Integer> disponibles = new HashSet<>();
        for (Despensa item : despensaRepository.buscarIngredientesDisponibles(
                idUsuario, LocalDate.now())) {
            disponibles.add(item.getIngrediente().getId());
        }

        // recetas con sus ingredientes (JOIN FETCH) guardadas por id
        Map<Integer, Receta> recetasPorId = new HashMap<>();
        for (Receta receta : recetaRepository.buscarRecetasConIngredientes()) {
            recetasPorId.put(receta.getId(), receta);
        }

        // construir la respuesta
        for (ICoincidenciaDespensaProyeccion fila : coincidencias) {

            Receta receta = recetasPorId.get(fila.getIdReceta());

            List<IngredienteResponse> faltantes = new ArrayList<>();
            for (IngredientesReceta ir : receta.getIngredientesReceta()) {
                if (!disponibles.contains(ir.getIngrediente().getId())) {
                    faltantes.add(ingredienteMapper.toDto(ir.getIngrediente()));
                }
            }

            resultados.add(new BuscarPorIngredientesResponse(
                    recetaMapper.aResponse(receta),
                    fila.getCoincidentes().intValue(),
                    fila.getTotal().intValue(),
                    fila.getPorcentaje(),
                    faltantes
            ));
        }

        return resultados;
    }

    // GET - Consultar receta por ID con sus datos calculados
    @Override
    public RecetaDetalleResponse obtenerRecetaPorId(Integer id) {

        // buscar la receta
        Receta receta = recetaRepository.findById(id)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe una receta con ID: " + id
                ));

        // obtener sus ingredientes
        List<IngredientesReceta> ingredientes = receta.getIngredientesReceta();

        // calcular el coste y las preferencias
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

        // calcular el coste por ración
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

        // ordenar los pasos
        List<Paso> pasosOrdenados = receta.getPasos()
                .stream()
                .sorted(Comparator.comparing(Paso::getNumero))
                .toList();

        // convertir los datos mediante MapStruct
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

        // buscar la receta
        Receta receta = recetaRepository.findById(id)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe una receta con ID: " + id
                ));

        // comprobar que el nombre no pertenezca a otra receta
        if (!receta.getNombre().equalsIgnoreCase(dto.nombre())
                && recetaRepository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ElementoDuplicado(
                    "Ya existe una receta con el nombre: " + dto.nombre()
            );
        }

        // comprobar que los pasos estén numerados sin huecos
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

        // preparar ingredientes y recalcular calorías
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

        // modificar los campos simples mediante MapStruct
        recetaMapper.actualizarReceta(dto, receta);

        // calcular calorías por ración
        int caloriasPorRacion = caloriasTotales.divide(
                BigDecimal.valueOf(dto.raciones()), 0, RoundingMode.HALF_UP
        ).intValueExact();
        receta.setCalorias(caloriasPorRacion);

        // guardar referencia a los registros hijos antiguos
        List<IngredientesReceta> ingredientesAntiguos =
                new ArrayList<>(receta.getIngredientesReceta());
        List<Paso> pasosAntiguos = new ArrayList<>(receta.getPasos());

        // quitar las relaciones antiguas de las colecciones gestionadas
        receta.getIngredientesReceta().clear();
        receta.getPasos().clear();

        // eliminarlas explícitamente
        ingredientesRecetaRepository.deleteAll(ingredientesAntiguos);
        pasoRepository.deleteAll(pasosAntiguos);
        ingredientesRecetaRepository.flush();
        pasoRepository.flush();


        receta.getIngredientesReceta().addAll(relaciones);
        for (PasoRecetaRequest item : dto.pasos()) {
            Paso paso = new Paso();
            paso.setNumero(item.numero());
            paso.setDescripcion(item.descripcion());
            paso.setReceta(receta);
            receta.getPasos().add(paso);
        }

        // guardar la receta; cascade = ALL persiste los nuevos hijos
        Receta recetaGuardada = recetaRepository.save(receta);
        log.info("Receta modificada: id={} nombre='{}'", recetaGuardada.getId(), recetaGuardada.getNombre());
        return recetaMapper.aResponse(recetaGuardada);
    }


    // PATCH - Retirar una receta (baja lógica)
    @Override
    @Transactional
    public void retirarReceta(Integer id) {

        // buscar la receta por su ID
        Receta receta = recetaRepository.findById(id)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe una receta con ID: " + id
                ));

        // comprobar si ya está retirada
        if (Boolean.TRUE.equals(receta.getRetirada())) {
            throw new ReglaNegocioException(
                    "La receta ya está retirada"
            );
        }

        // cambiar el estado de la receta
        receta.setRetirada(true);

        // guardar los cambios
        recetaRepository.save(receta);
        log.info("Receta retirada (baja lógica): id={}", id);
    }

}
