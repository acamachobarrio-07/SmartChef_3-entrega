package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.dtos.response.FilaColeccionResponse;
import es.safareyes.smartchef.entity.Coleccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IColeccionRepository extends JpaRepository<Coleccion, Integer> {

    // Mis colecciones con sus recetas
    // LEFT JOIN: también devuelve las colecciones vacías (RN-08)
    @Query("""
    SELECT new es.safareyes.smartchef.dtos.response.FilaColeccionResponse(
        c.id,
        c.nombre,
        r.id,
        r.nombre,
        r.tiempo,
        r.eRecetaDificultad,
        r.retirada
    )
    FROM Coleccion c
    LEFT JOIN c.recetas r
    WHERE c.usuario.id = :idUsuario
    ORDER BY c.nombre ASC, r.nombre ASC
    """)
    List<FilaColeccionResponse> obtenerColeccionesConRecetasPorUsuario(@Param("idUsuario") Integer idUsuario);

    //colección concreta de un usuario por su nombre

    Optional<Coleccion> findByUsuarioIdAndNombreIgnoreCase(Integer idUsuario, String nombre);
}