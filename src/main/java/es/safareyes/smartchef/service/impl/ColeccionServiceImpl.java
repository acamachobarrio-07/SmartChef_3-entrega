package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.response.ColeccionResponse;
import es.safareyes.smartchef.dtos.response.FilaColeccionResponse;
import es.safareyes.smartchef.entity.Coleccion;
import es.safareyes.smartchef.entity.Receta;
import es.safareyes.smartchef.entity.Usuario;
import es.safareyes.smartchef.exception.ElementoDuplicado;
import es.safareyes.smartchef.exception.ReglaNegocioException;
import es.safareyes.smartchef.exception.RelacionNoEncontrada;
import es.safareyes.smartchef.mapper.IColeccionMapper;
import es.safareyes.smartchef.repository.IColeccionRepository;
import es.safareyes.smartchef.repository.IRecetaRepository;
import es.safareyes.smartchef.repository.IUsuarioRepository;
import es.safareyes.smartchef.service.IColeccionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class ColeccionServiceImpl implements IColeccionService {

    private IColeccionRepository coleccionRepository;
    private IUsuarioRepository usuarioRepository;
    private IColeccionMapper coleccionMapper;
    private IRecetaRepository recetaRepository;

    // GET - Colecciones del usuario con sus recetas
    @Override
    public List<ColeccionResponse> obtenerColecciones(Integer idUsuario) {

        // comprobar que el usuario existe
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new RelacionNoEncontrada(
                    "No existe el usuario con ID: " + idUsuario
            );
        }

        // obtener las filas
        List<FilaColeccionResponse> filas =
                coleccionRepository.obtenerColeccionesConRecetasPorUsuario(idUsuario);

        // 3. agrupar las filas por colección
        //    LinkedHashMap mantiene el orden de la consulta (por nombre)
        Map<Integer, ColeccionResponse> colecciones = new LinkedHashMap<>();

        for (FilaColeccionResponse fila : filas) {

            // Si es la primera fila de esta colección, la creamos con la lista vacía
            ColeccionResponse coleccion = colecciones.get(fila.idColeccion());

            if (coleccion == null) {
                coleccion = new ColeccionResponse(
                        fila.idColeccion(),
                        fila.coleccion(),
                        new ArrayList<>()
                );
                colecciones.put(fila.idColeccion(), coleccion);
            }

            // Si la colección está vacía, el LEFT JOIN trae la receta a null
            if (fila.idReceta() != null) {
                coleccion.recetas().add(coleccionMapper.aRecetaResponse(fila));
            }
        }

        // 4. Devolver las colecciones como lista
        return new ArrayList<>(colecciones.values());
    }

    // PUT - Guardar receta en colección, creándola si no existe (RN-08)
    @Override
    @Transactional
    public ColeccionResponse guardarReceta(Integer idUsuario, String nombreColeccion, Integer idReceta) {

        // comprobar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe el usuario con ID: " + idUsuario
                ));

        //comprobar que la receta existe
        Receta receta = recetaRepository.findById(idReceta)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe la receta con ID: " + idReceta
                ));

        //comprobar si la receta esta retirada
        if (Boolean.TRUE.equals(receta.getRetirada())) {
            throw new ReglaNegocioException(
                    "La receta " + receta.getNombre() + " está retirada y no se puede guardar"
            );
        }

        Optional<Coleccion> existente =
                coleccionRepository.findByUsuarioIdAndNombreIgnoreCase(idUsuario, nombreColeccion);

        Coleccion coleccion;

        if (existente.isPresent()) {
            // ya existe esta colección por lo que nos quedamos con esta
            coleccion = existente.get();
        } else {
            // no existe esta coleccion, la creamos y la guardamos
            coleccion = new Coleccion();
            coleccion.setNombre(nombreColeccion.trim());
            coleccion.setUsuario(usuario);
            coleccion = coleccionRepository.save(coleccion);
            log.info("Colección '{}' creada para el usuario {}", coleccion.getNombre(), idUsuario);
        }

        //comprobar si la receta ya está en la colección

        boolean yaGuardada = coleccion.getRecetas().stream()
                .anyMatch(r -> r.getId().equals(idReceta));

        if (yaGuardada) {
            throw new ElementoDuplicado(
                    "La receta ya está guardada en la colección " + coleccion.getNombre()
            );
        }

        //añadir la receta
        coleccion.getRecetas().add(receta);

        log.info("Receta {} guardada en la colección '{}' del usuario {}", idReceta, coleccion.getNombre(), idUsuario);

        //devolver la receta convertida con el mapper
        return coleccionMapper.aResponse(coleccion);
    }
}