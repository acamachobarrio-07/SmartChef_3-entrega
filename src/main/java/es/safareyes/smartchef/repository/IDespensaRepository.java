package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.entity.Despensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.time.LocalDate;
import java.util.List;

public interface IDespensaRepository extends JpaRepository<Despensa, Integer> {

    // Obtener ingredientes disponibles y no caducados de un usuario
    @Query("""
        SELECT d
        FROM Despensa d
        JOIN FETCH d.ingrediente
        WHERE d.usuario.id = :idUsuario
          AND d.cantidad > 0
          AND (d.caducidad IS NULL OR d.caducidad >= :hoy)
        ORDER BY d.caducidad ASC
        """)
    List<Despensa> buscarIngredientesDisponibles(
            @Param("idUsuario") Integer idUsuario,
            @Param("hoy") LocalDate hoy
    );


    // Obtener la despensa de un usuario ordenada por caducidad
    @Query("""
            SELECT d
            FROM Despensa d
            JOIN FETCH d.ingrediente
            WHERE d.usuario.id = :idUsuario
            ORDER BY
                CASE WHEN d.caducidad IS NULL THEN 1 ELSE 0 END,
                d.caducidad ASC,
                d.id ASC
            """)
    List<Despensa> listarPorUsuarioOrdenadoPorCaducidad(
            @Param("idUsuario") Integer idUsuario
    );

    // Buscar un ingrediente concreto en la despensa de un usuario
    Optional<Despensa> findByUsuarioIdAndIngredienteId(
            Integer idUsuario,
            Integer idIngrediente
    );

}