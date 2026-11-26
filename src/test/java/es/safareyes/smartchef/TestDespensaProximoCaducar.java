package es.safareyes.smartchef;

import es.safareyes.smartchef.entity.Despensa;
import es.safareyes.smartchef.repository.IDespensaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

// Endpoint 8 - GET /api/v1/despensa (lo que caduca antes, primero)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestDespensaProximoCaducar {

    @Autowired
    private IDespensaRepository repository;

    @Test
    void obtenerDespensaOrdenadaPorCaducidad() {
        List<Despensa> despensa = repository.listarPorUsuarioOrdenadoPorCaducidad(2);

        despensa.forEach(d -> System.out.println(
                d.getIngrediente().getNombre() + " - " + d.getCantidad() + " - caduca: " + d.getCaducidad()));
    }
}