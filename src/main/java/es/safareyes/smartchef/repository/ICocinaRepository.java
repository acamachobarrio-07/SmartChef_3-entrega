package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.dtos.response.SemanaCaloriasResponse;
import es.safareyes.smartchef.entity.Cocina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ICocinaRepository extends JpaRepository<Cocina, Integer> {
    //semana por dias con calorias totales
    @Query("""
SELECT new es.safareyes.smartchef.dtos.response.SemanaCaloriasResponse(            CAST(c.fecha AS LocalDate),
            CAST(FUNCTION('TO_CHAR', c.fecha, 'TMDay') AS String),
            COUNT(r.id),
            CAST(COALESCE(SUM(r.calorias), 0) AS Long)
        )
        FROM Cocina c
        JOIN c.receta r
        WHERE c.usuario.id = :idUsuario
          AND c.fecha >= :desde
          AND c.fecha < :hasta
        GROUP BY
            CAST(c.fecha AS LocalDate),
            CAST(FUNCTION('TO_CHAR', c.fecha, 'TMDay') AS String)
        ORDER BY CAST(c.fecha AS LocalDate) ASC
        """)
    List<SemanaCaloriasResponse> obtenerResumenSemanal(
            @Param("idUsuario") Integer idUsuario,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
