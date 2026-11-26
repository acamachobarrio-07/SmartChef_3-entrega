package es.safareyes.smartchef;

import es.safareyes.smartchef.dtos.response.RecetaResponse;
import es.safareyes.smartchef.entity.ERecetaDificultad;
import es.safareyes.smartchef.repository.IRecetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

// Endpoint 1 - GET /api/v1/recetas (catálogo con filtros)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestRecetasFiltros {

    @Autowired
    private IRecetaRepository repository;

    @Test
    void buscarPorFiltros() {
        // Fáciles, de 30 minutos o menos, vegetarianas y sin gluten
        Page<RecetaResponse> resultados = repository.buscarConFiltros(
                30,
                ERecetaDificultad.FACIL,
                true,
                true,
                null,
                PageRequest.of(0, 10)
        );

        System.out.println("Total: " + resultados.getTotalElements());
        resultados.forEach(System.out::println);
    }
}
