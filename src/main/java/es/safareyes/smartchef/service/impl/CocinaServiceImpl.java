package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.response.ComidaResponse;
import es.safareyes.smartchef.dtos.response.DiaSemanaResponse;
import es.safareyes.smartchef.dtos.response.SemanaCaloriasResponse;
import es.safareyes.smartchef.dtos.response.SemanaResponse;
import es.safareyes.smartchef.entity.Cocina;
import es.safareyes.smartchef.exception.RelacionNoEncontrada;
import es.safareyes.smartchef.repository.ICocinaRepository;
import es.safareyes.smartchef.repository.IUsuarioRepository;
import es.safareyes.smartchef.service.ICocinaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CocinaServiceImpl implements ICocinaService {

    private ICocinaRepository cocinaRepository;
    private IUsuarioRepository usuarioRepository;

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

        // 3. Calorías por día (consulta de la Fase 2)
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
        long caloriasSemana = 0;

        for (int i = 0; i < 7; i++) {

            LocalDate actual = lunes.plusDays(i);

            // Comidas de este día
            List<ComidaResponse> comidas = new ArrayList<>();
            for (Cocina registro : registros) {
                if (registro.getFecha().toLocalDate().equals(actual)) {
                    comidas.add(new ComidaResponse(
                            registro.getId(),
                            convertirMomento(registro.getTipo()),
                            registro.getReceta().getId(),
                            registro.getReceta().getNombre(),
                            registro.getReceta().getCalorias()
                    ));
                }
            }

            long calorias = caloriasPorDia.getOrDefault(actual, 0L);
            String nombreDia = actual.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.of("es", "ES"));

            dias.add(new DiaSemanaResponse(actual, nombreDia, calorias, comidas));
            caloriasSemana += calorias;
        }

        // 6. Devolver la semana
        return new SemanaResponse(lunes, domingo, caloriasSemana, dias);
    }

    // Convierte el número guardado en "tipo" en el momento del día
    private String convertirMomento(Integer tipo) {
        if (tipo == null) {
            return null;
        }
        return switch (tipo) {
            case 1 -> "DESAYUNO";
            case 2 -> "COMIDA";
            case 3 -> "CENA";
            default -> "DESCONOCIDO";
        };
    }
}