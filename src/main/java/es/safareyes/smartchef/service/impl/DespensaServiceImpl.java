package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.request.DespensaRequest;
import es.safareyes.smartchef.dtos.response.DespensaResponse;
import es.safareyes.smartchef.entity.Despensa;
import es.safareyes.smartchef.exception.RelacionNoEncontrada;
import es.safareyes.smartchef.mapper.DespensaMapper;
import es.safareyes.smartchef.repository.IDespensaRepository;
import es.safareyes.smartchef.repository.IUsuarioRepository;
import es.safareyes.smartchef.service.IDespensaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class DespensaServiceImpl implements IDespensaService {

    private IDespensaRepository despensaRepository;
    private IUsuarioRepository usuarioRepository;
    private DespensaMapper despensaMapper;

    @Override
    public List<DespensaResponse> obtenerDespensa(DespensaRequest datos) {

        // Comprobamos que el usuario existe
        if (!usuarioRepository.existsById(datos.idUsuario())) {
            throw new RelacionNoEncontrada(
                    "No existe el usuario con ID: " + datos.idUsuario()
            );
        }

        // Obtenemos su despensa ordenada por fecha de caducidad
        List<Despensa> despensa =
                despensaRepository.listarPorUsuarioOrdenadoPorCaducidad(
                        datos.idUsuario()
                );

        // Convertimos las entidades a DTOs
        return despensaMapper.toDtoList(despensa);
    }
}