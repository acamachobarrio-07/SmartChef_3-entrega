package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.response.InformeRecetasResponse;
import es.safareyes.smartchef.dtos.response.RecetaPopularResponse;
import es.safareyes.smartchef.repository.IRecetaRepository;
import es.safareyes.smartchef.service.IInformeService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class InformeServiceImpl implements IInformeService {

    // cuántas recetas salen en cada ranking
    private static final int TOP_RECETAS = 10;

    private IRecetaRepository recetaRepository;

    // GET - informe de recetas populares
    @Override
    public InformeRecetasResponse recetasPopulares(LocalDate desde, LocalDate hasta) {

        // usamos pageable para mostrar los dos primeros de cada uni
        Pageable top = PageRequest.of(0, TOP_RECETAS);

        // rango con la hora:
        // - desde las 00:00 del primer día
        // - hasta las 00:00 del día siguiente al último (sin incluirlo)
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();

        // tenemos los dos ranking:
        // - las mas cocinadas
        // - las mas guardadas
        List<RecetaPopularResponse> masCocinadas =
                recetaRepository.buscarMasCocinadas(inicio, fin, top);

        List<RecetaPopularResponse> masGuardadas =
                recetaRepository.buscarMasGuardadas(top);

        // devolvemos el informe
        return new InformeRecetasResponse(desde, hasta, masCocinadas, masGuardadas);
    }
}
