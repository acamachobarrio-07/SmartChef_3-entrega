package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.entity.Ingredientes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IIngredientesRepository extends JpaRepository<Ingredientes, Integer> {

    //Catálogo de ingredientes disponibles (público)

    List<Ingredientes> findAllByOrderByNombreAsc();

}