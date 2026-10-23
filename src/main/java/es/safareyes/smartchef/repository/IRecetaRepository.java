package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.dtos.*;
import es.safareyes.smartchef.entity.ERecetaDificultad;
import es.safareyes.smartchef.entity.Receta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface IRecetaRepository extends JpaRepository<Receta, Integer> {

    //Catálogo con filtros



    @Query("""
    SELECT new es.safareyes.smartchef.dtos.DatosFiltradoReceta(
        r.id,
        r.nombre,
        r.descripcion,
        r.raciones,
        r.tiempo,
        r.calorias,
        r.eRecetaDificultad
    )
    FROM Receta r
    WHERE (:tiempoMax IS NULL OR r.tiempo <= :tiempoMax)
      AND (:dificultad IS NULL OR r.eRecetaDificultad = :dificultad)

      AND (:vegetariano IS NULL OR :vegetariano = false
          OR NOT EXISTS (
              SELECT ir.id
              FROM IngredientesReceta ir
              JOIN ir.ingrediente i
              LEFT JOIN i.alergias a
                  ON a.ePreferenciasTipo =
                     es.safareyes.smartchef.entity.EPreferenciasTipo.VEGETARIANO
              WHERE ir.receta = r AND a.id IS NULL
          ))

      AND (:sinGluten IS NULL OR :sinGluten = false
          OR NOT EXISTS (
              SELECT ir.id
              FROM IngredientesReceta ir
              JOIN ir.ingrediente i
              LEFT JOIN i.alergias a
                  ON a.ePreferenciasTipo =
                     es.safareyes.smartchef.entity.EPreferenciasTipo.SINGLUTEN
              WHERE ir.receta = r AND a.id IS NULL
          ))
      
      AND (
          :economico IS NULL
          OR :economico = false
          OR (
              r.raciones > 0
              AND (
                  SELECT SUM(
                      CASE
                          WHEN i.eIngredientesTipoCantidad =
                               es.safareyes.smartchef.entity.EIngredientesTipoCantidad.UNIDADES
                          THEN ir.cantidad * i.precio
                          ELSE (ir.cantidad * i.precio) / 100
                      END
                  )
                  FROM IngredientesReceta ir
                  JOIN ir.ingrediente i
                  WHERE ir.receta = r
              ) <= r.raciones * 2.50
          )
      )
      

    ORDER BY r.nombre ASC, r.id ASC
    """)
    Page<DatosFiltradoReceta> buscarConFiltros(
            @Param("tiempoMax") Integer tiempoMax,
            @Param("dificultad") ERecetaDificultad dificultad,
            @Param("vegetariano") Boolean vegetariano,
            @Param("sinGluten") Boolean sinGluten,
            @Param("economico") Boolean economico,
            Pageable pageable
    );



    //Detalle receta con ingredientes

    @Query("""
    SELECT new es.safareyes.smartchef.dtos.DatosRecetaDetalle(
        r.id,
        r.nombre,
        r.calorias,
        r.tiempo,
        r.eRecetaDificultad,
        r.descripcion,
        listagg(
            DISTINCT CONCAT(i.nombre, ' (', CAST(ir.cantidad AS String), ' ',
                            CAST(i.eIngredientesTipoCantidad AS String), ')'),
            ', '
        ),
        listagg(
            DISTINCT CONCAT(CAST(p.numero AS String), '. ', p.descripcion),
            ' | '
        ) WITHIN GROUP (ORDER BY CONCAT(CAST(p.numero AS String), '. ', p.descripcion) ASC)
    )
    FROM Receta r
    JOIN r.ingredientesReceta ir
    JOIN ir.ingrediente i
    LEFT JOIN r.pasos p
    WHERE r.id = :idReceta
    GROUP BY r.id, r.nombre, r.calorias, r.tiempo, r.eRecetaDificultad, r.descripcion
    """)
    List<DatosRecetaDetalle> obtenerDetalleRecetaConIngredientes(@Param("idReceta") Integer idReceta);

    //top 5 recetas mas cocinadas y mas guardadas

    @Query("""
    SELECT new es.safareyes.smartchef.dtos.DatosRecetasPopulares(
        r.nombre,
        r.eRecetaDificultad,
        r.tiempo,
        COUNT(DISTINCT c.id),
        COUNT(DISTINCT col.id)
    )
    FROM Receta r
    LEFT JOIN Cocina c ON c.receta.id = r.id AND c.usuario.id = :idUsuario
    LEFT JOIN r.colecciones col
    WHERE c.id IS NOT NULL OR col.id IS NOT NULL
    GROUP BY r.id, r.nombre, r.eRecetaDificultad, r.tiempo
    ORDER BY (COUNT(DISTINCT c.id) + COUNT(DISTINCT col.id)) DESC
    """)
    List<DatosRecetasPopulares> findTop5ByUsuario(@Param("idUsuario") Integer idUsuario);
}

    //@Query("""
    //SELECT p FROM Prestamo p
    //WHERE p.estado = :estado AND p.fechaPrestamo < :limite
    //ORDER BY p.fechaPrestamo ASC
    //""")
    //List<Prestamo> buscarRetrasados(@Param("estado") EstadoPrestamo estado,
    //@Param("limite") LocalDate limite);

    /*
    * 1º METODO DE CONSULTA
    * extender JPA podemos hacer consultas por extender el nombre completo
    * las consultas nos va a devolver una lista
    * select from continentes where nombre like %américa% and orden<4
    * ejemplo consulta: List<Continente>findAllByNombreContiningAndORdenLessThan(String nombre, Int orden);
    * en el test probamos el metodo al test le tenemos que pasar las variables de entorno
     * */

    /*
    * 2º METODO DE CONSULTA MEZCLA SQL Y JAVA (JPQL)
    * @Query("select e from Especie where e.tipoAgua.nombre=:nombreAgua")
    * List<Especie> buscarEspeciePorNombreTipoAgua(@Param ("nombreAgua")String nombre)
    * una vez que lo tengo construido probamos en el test!
    * */

    /*
    * 3º METODO SQL NATIVO
    * @Query (value = select e.* from especie e join cat_tipo_agua cta on cta.id = e.id_tipo_agua where cta.nombre=:agua, nativeQuery=true)
    * List<Continente> buscarPorAgua(@Param("agua" String nombreAgua)
    * */

