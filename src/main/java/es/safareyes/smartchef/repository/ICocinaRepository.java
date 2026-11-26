package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.dtos.response.SemanaCaloriasResponse;
import es.safareyes.smartchef.entity.Cocina;
import es.safareyes.smartchef.entity.ECocinaTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ICocinaRepository extends JpaRepository<Cocina, Integer> {

    // Semana por días con calorías totales
    @Query("""
        SELECT new es.safareyes.smartchef.dtos.response.SemanaCaloriasResponse(
            CAST(c.fecha AS LocalDate),
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

    // Registros de una semana con su receta (detalle de cada día)
    @Query("""
        SELECT c
        FROM Cocina c
        JOIN FETCH c.receta
        WHERE c.usuario.id = :idUsuario
          AND c.fecha >= :desde
          AND c.fecha < :hasta
        ORDER BY c.fecha ASC, c.tipo ASC
        """)
    List<Cocina> buscarRegistrosSemana(
            @Param("idUsuario") Integer idUsuario,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    // RN-07: ¿ya hay un registro de este usuario, con este tipo, entre dos instantes (un día)?
    boolean existsByUsuarioIdAndTipoAndFechaBetween(
            Integer idUsuario,
            ECocinaTipo tipo,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // Q3: SQL NATIVO con función propia de PostgreSQL.
    // DATE_TRUNC('week', fecha) devuelve el LUNES a las 00:00 de la semana de esa fecha,
    // así que comparamos "la semana del registro" con "la semana del día pedido".
    @Query(nativeQuery = true, value = """
        SELECT COALESCE(SUM(r.calorias), 0)
        FROM cocina c
        JOIN receta r ON r.id = c.id_receta
        WHERE c.id_usuario = :idUsuario
          AND DATE_TRUNC('week', c.fecha) = DATE_TRUNC('week', CAST(:dia AS timestamp))
        """)
    Long obtenerCaloriasSemana(
            @Param("idUsuario") Integer idUsuario,
            @Param("dia") LocalDate dia
    );
}