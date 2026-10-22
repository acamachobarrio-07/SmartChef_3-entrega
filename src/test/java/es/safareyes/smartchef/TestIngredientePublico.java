package es.safareyes.smartchef;

import es.safareyes.smartchef.entity.Ingredientes;
import es.safareyes.smartchef.repository.IIngredientesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestIngredientePublico {

    @Autowired
    private IIngredientesRepository repository;

    @Test
    void obtenerIngredientesDisponibles() {
        List<Ingredientes> ingredientes = repository.findAllByOrderByNombreAsc();  }

}
