package es.safareyes.smartchef.repository;

import es.safareyes.smartchef.entity.EEstadoLista;
import es.safareyes.smartchef.entity.ListaCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IListaCompraRepository extends JpaRepository<ListaCompra, Integer> {

    // Obtener los ingredientes de la lista de un usuario según su estado
    List<ListaCompra> findByUsuarioIdAndEstadoOrderByFechaAsc(
            Integer idUsuario,
            EEstadoLista estado
    );

    // Buscar un ingrediente concreto dentro de la lista abierta de un usuario
    Optional<ListaCompra> findByUsuarioIdAndIngredienteIdAndEstado(
            Integer idUsuario,
            Integer idIngrediente,
            EEstadoLista estado
    );
}
