package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {
}
