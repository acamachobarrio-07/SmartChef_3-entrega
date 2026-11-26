package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.request.ActualizarDespensaRequest;
import es.safareyes.smartchef.dtos.request.DespensaRequest;
import es.safareyes.smartchef.dtos.response.DespensaResponse;
import es.safareyes.smartchef.entity.Despensa;
import es.safareyes.smartchef.entity.Ingredientes;
import es.safareyes.smartchef.entity.Usuario;
import es.safareyes.smartchef.exception.RelacionNoEncontrada;
import es.safareyes.smartchef.mapper.IDespensaMapper;
import es.safareyes.smartchef.repository.IDespensaRepository;
import es.safareyes.smartchef.repository.IIngredientesRepository;
import es.safareyes.smartchef.repository.IUsuarioRepository;
import es.safareyes.smartchef.service.IDespensaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DespensaServiceImpl implements IDespensaService {

    private IDespensaRepository despensaRepository;
    private IUsuarioRepository usuarioRepository;
    private IIngredientesRepository ingredientesRepository;
    private IDespensaMapper despensaMapper;

    // GET - Consultar la despensa de un usuario
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

    // PUT - Añadir o actualizar un ingrediente de la despensa
    @Override
    @Transactional
    public DespensaResponse guardarEnDespensa(
            Integer idUsuario,
            Integer idIngrediente,
            ActualizarDespensaRequest datos) {

        // 1. Comprobar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe el usuario con ID: " + idUsuario
                ));

        // 2. Comprobar que el ingrediente existe en el catálogo
        Ingredientes ingrediente = ingredientesRepository.findById(idIngrediente)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe el ingrediente con ID: " + idIngrediente
                ));

        // 3. Buscar si el ingrediente ya está en la despensa del usuario
        Optional<Despensa> existente =
                despensaRepository.findByUsuarioIdAndIngredienteId(
                        idUsuario,
                        idIngrediente
                );

        // 4. Cantidad 0: se elimina de la despensa (si estaba)
        if (datos.cantidad().compareTo(BigDecimal.ZERO) == 0) {
            existente.ifPresent(despensaRepository::delete);
            log.info("Ingrediente {} eliminado de la despensa del usuario {}", idIngrediente, idUsuario);
            return null;
        }

        // 5. Si no estaba, creamos una línea nueva para ese usuario e ingrediente
        Despensa despensa = existente.orElseGet(() -> {
            Despensa nueva = new Despensa();
            nueva.setUsuario(usuario);
            nueva.setIngrediente(ingrediente);
            return nueva;
        });

        // 6. Guardar la cantidad y la caducidad nuevas
        despensa.setCantidad(datos.cantidad());
        despensa.setCaducidad(datos.caducidad());

        // 7. Guardar y devolver el DTO
        Despensa guardada = despensaRepository.save(despensa);
        log.info("Despensa del usuario {}: ingrediente {} con cantidad {}", idUsuario, idIngrediente, guardada.getCantidad());
        return despensaMapper.toDto(guardada);
    }
}