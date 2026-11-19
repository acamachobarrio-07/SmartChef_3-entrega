package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.response.ColeccionResponse;
import es.safareyes.smartchef.dtos.response.FilaColeccionResponse;
import es.safareyes.smartchef.exception.RelacionNoEncontrada;
import es.safareyes.smartchef.mapper.IColeccionMapper;
import es.safareyes.smartchef.repository.IColeccionRepository;
import es.safareyes.smartchef.repository.IUsuarioRepository;
import es.safareyes.smartchef.service.IColeccionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ColeccionServiceImpl implements IColeccionService {

    private IColeccionRepository coleccionRepository;
    private IUsuarioRepository usuarioRepository;
    private IColeccionMapper coleccionMapper;

    // GET - Colecciones del usuario con sus recetas
    @Override
    public List<ColeccionResponse> obtenerColecciones(Integer idUsuario) {

        // 1. Comprobar que el usuario existe
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new RelacionNoEncontrada(
                    "No existe el usuario con ID: " + idUsuario
            );
        }

        // 2. Obtener las filas: una por cada pareja colección-receta
        List<FilaColeccionResponse> filas =
                coleccionRepository.obtenerColeccionesConRecetasPorUsuario(idUsuario);

        // 3. Agrupar las filas por colección
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
}