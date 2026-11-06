package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.entity.Ingredientes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IIngredientesRepository extends JpaRepository<Ingredientes, Integer> {

    //Catálogo de ingredientes disponibles (público)

    List<Ingredientes> findAllByOrderByNombreAsc();

    // Buscar ingredientes por nombre con paginación
    Page<Ingredientes> findByNombreContainingIgnoreCase(
            String texto,
            Pageable pageable
    );

}