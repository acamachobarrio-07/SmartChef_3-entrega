package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.entity.Ingredientes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IIngredientesRepository extends JpaRepository<Ingredientes, Integer> {

    // Buscar ingredientes por nombre con paginación
    Page<Ingredientes> findByNombreContainingIgnoreCase(
            String texto,
            Pageable pageable
    );

}