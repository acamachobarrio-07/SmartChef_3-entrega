package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.entity.Preferencias;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPreferenciasRepository extends JpaRepository<Preferencias, Integer> {
}
