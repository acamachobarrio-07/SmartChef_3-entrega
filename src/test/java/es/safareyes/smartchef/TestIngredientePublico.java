package es.safareyes.smartchef;

import es.safareyes.smartchef.entity.Ingredientes;
import es.safareyes.smartchef.repository.IIngredientesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

// Endpoint 6 - GET /api/v1/ingredientes (catálogo paginado)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestIngredientePublico {

    @Autowired
    private IIngredientesRepository repository;

    @Test
    void buscarIngredientesPorNombre() {
        Page<Ingredientes> ingredientes = repository.findByNombreContainingIgnoreCase(
                "tomate",
                PageRequest.of(0, 10, Sort.by("nombre").ascending())
        );

        System.out.println("Total: " + ingredientes.getTotalElements());
        ingredientes.forEach(i -> System.out.println(i.getId() + " - " + i.getNombre()));
    }
}