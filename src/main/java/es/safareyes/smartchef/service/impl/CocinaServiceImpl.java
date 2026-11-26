package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.request.RegistroRequest;
import es.safareyes.smartchef.dtos.response.ComidaResponse;
import es.safareyes.smartchef.dtos.response.DiaSemanaResponse;
import es.safareyes.smartchef.dtos.response.RegistroResponse;
import es.safareyes.smartchef.dtos.response.SemanaCaloriasResponse;
import es.safareyes.smartchef.dtos.response.SemanaResponse;
import es.safareyes.smartchef.entity.Cocina;
import es.safareyes.smartchef.entity.IngredientesReceta;
import es.safareyes.smartchef.entity.Receta;
import es.safareyes.smartchef.entity.Usuario;
import es.safareyes.smartchef.exception.ElementoDuplicado;
import es.safareyes.smartchef.exception.RelacionNoEncontrada;
import es.safareyes.smartchef.repository.ICocinaRepository;
import es.safareyes.smartchef.repository.IDespensaRepository;
import es.safareyes.smartchef.repository.IRecetaRepository;
import es.safareyes.smartchef.repository.IUsuarioRepository;
import es.safareyes.smartchef.service.ICocinaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class CocinaServiceImpl implements ICocinaService {

    private ICocinaRepository cocinaRepository;
    private IUsuarioRepository usuarioRepository;
    private IRecetaRepository recetaRepository;
    private IDespensaRepository despensaRepository;

    // GET - Semana con comidas y calorías por día (endpoint 15)
    @Override
    public SemanaResponse obtenerSemana(Integer idUsuario, LocalDate fecha) {

        // 1. Comprobar que el usuario existe (404)
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new RelacionNoEncontrada("No existe el usuario con ID: " + idUsuario);
        }

        // 2. Calcular el lunes y el domingo (si no llega fecha, la semana actual)
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        LocalDate lunes = dia.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);

        // Las consultas usan LocalDateTime: desde el lunes a las 00:00
        // hasta el lunes siguiente a las 00:00 (sin incluirlo)
        LocalDateTime desde = lunes.atStartOfDay();
        LocalDateTime hasta = lunes.plusDays(7).atStartOfDay();

        // 3. Calorías por día (Q5: SELECT new con record)
        List<SemanaCaloriasResponse> totales =
                cocinaRepository.obtenerResumenSemanal(idUsuario, desde, hasta);

        Map<LocalDate, Long> caloriasPorDia = new HashMap<>();
        for (SemanaCaloriasResponse total : totales) {
            caloriasPorDia.put(total.dia(), total.caloriasTotales());
        }

        // 4. Registros de la semana con su receta
        List<Cocina> registros =
                cocinaRepository.buscarRegistrosSemana(idUsuario, desde, hasta);

        // 5. Construir los 7 días (los que no tienen registros, vacíos y a 0)
        List<DiaSemanaResponse> dias = new ArrayList<>();

        for (int i = 0; i < 7; i++) {

            LocalDate actual = lunes.plusDays(i);

            // Comidas de este día
            List<ComidaResponse> comidas = new ArrayList<>();
            for (Cocina registro : registros) {
                if (registro.getFecha().toLocalDate().equals(actual)) {
                    comidas.add(new ComidaResponse(
                            registro.getId(),
                            registro.getTipo() != null ? registro.getTipo().name() : null,
                            registro.getReceta().getId(),
                            registro.getReceta().getNombre(),
                            registro.getReceta().getCalorias(),
                            registro.getReceta().getRetirada()
                    ));
                }
            }

            long calorias = caloriasPorDia.getOrDefault(actual, 0L);
            String nombreDia = actual.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.of("es", "ES"));

            dias.add(new DiaSemanaResponse(actual, nombreDia, calorias, comidas));
        }

        // Total de la semana con la consulta nativa (Q3: DATE_TRUNC 'week')
        long caloriasSemana = cocinaRepository.obtenerCaloriasSemana(idUsuario, lunes);

        // 6. Devolver la semana
        return new SemanaResponse(lunes, domingo, caloriasSemana, dias);
    }

    // POST - Registrar lo que ha cocinado el usuario (endpoint 14, RN-07)
    @Override
    @Transactional
    public RegistroResponse registrar(Integer idUsuario, RegistroRequest datos) {

        // 1. Comprobar que el usuario existe (404)
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe el usuario con ID: " + idUsuario
                ));

        // 2. Comprobar que la receta existe (404)
        Receta receta = recetaRepository.findById(datos.idReceta())
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe la receta con ID: " + datos.idReceta()
                ));

        // 3. RN-07: un registro por día y tipo de comida (409)
        //    El día va desde las 00:00 hasta las 23:59:59
        LocalDate dia = datos.fecha().toLocalDate();
        LocalDateTime inicioDia = dia.atStartOfDay();
        LocalDateTime finDia = dia.atTime(LocalTime.MAX);

        if (cocinaRepository.existsByUsuarioIdAndTipoAndFechaBetween(
                idUsuario, datos.tipo(), inicioDia, finDia)) {
            throw new ElementoDuplicado(
                    "Ya hay un registro de " + datos.tipo() + " el día " + dia
            );
        }

        // 4. RN-07: si se pide, descontar de la despensa (nunca por debajo de 0)
        if (Boolean.TRUE.equals(datos.descontarDespensa())) {
            descontarDeDespensa(idUsuario, receta);
            log.info("Despensa del usuario {} descontada con la receta {}", idUsuario, receta.getId());
        }

        // 5. Crear y guardar el registro
        Cocina registro = new Cocina();
        registro.setUsuario(usuario);
        registro.setReceta(receta);
        registro.setTipo(datos.tipo());
        registro.setFecha(datos.fecha());

        Cocina guardado = cocinaRepository.save(registro);
        log.info("Registro creado: id={} usuario={} receta={} {} {}",
                guardado.getId(), idUsuario, receta.getId(), guardado.getTipo(), dia);

        // 6. Devolver el registro creado
        return new RegistroResponse(
                guardado.getId(),
                guardado.getFecha(),
                guardado.getTipo(),
                receta.getId(),
                receta.getNombre(),
                receta.getCalorias(),
                receta.getRetirada()
        );
    }

    // Resta de la despensa los ingredientes de la receta, sin bajar nunca de 0
    private void descontarDeDespensa(Integer idUsuario, Receta receta) {

        for (IngredientesReceta ir : receta.getIngredientesReceta()) {

            despensaRepository
                    .findByUsuarioIdAndIngredienteId(idUsuario, ir.getIngrediente().getId())
                    .ifPresent(despensa -> {
                        BigDecimal resto = despensa.getCantidad().subtract(ir.getCantidad());
                        // max(): si el resto es negativo, se queda en 0
                        despensa.setCantidad(resto.max(BigDecimal.ZERO));
                    });
        }
    }
}