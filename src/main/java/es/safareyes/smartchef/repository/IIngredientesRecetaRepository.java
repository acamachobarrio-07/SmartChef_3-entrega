package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.entity.IngredientesReceta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IIngredientesRecetaRepository extends JpaRepository<IngredientesReceta, Integer> {
}
