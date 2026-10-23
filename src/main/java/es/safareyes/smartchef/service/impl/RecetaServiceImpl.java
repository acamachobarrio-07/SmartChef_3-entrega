
package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.request.RecetasRequest;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import es.safareyes.smartchef.mapper.RecetaMapper;
import es.safareyes.smartchef.repository.IRecetaRepository;
import es.safareyes.smartchef.service.RecetaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecetaServiceImpl implements RecetaService {

    private final IRecetaRepository recetaRepository;
    private final RecetaMapper recetaMapper;


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


}
