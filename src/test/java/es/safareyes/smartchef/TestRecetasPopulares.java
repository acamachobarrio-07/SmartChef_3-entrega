package es.safareyes.smartchef;

import es.safareyes.smartchef.dtos.response.RecetaPopularResponse;
import es.safareyes.smartchef.repository.IRecetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestRecetasPopulares {

    @Autowired
    private IRecetaRepository repository;

    @Test
    void consultarRecetasMasCocinadas() {
        // Último mes, top 10
        LocalDateTime desde = LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime hasta = LocalDate.now().plusDays(1).atStartOfDay();

        List<RecetaPopularResponse> cocinadas =
                repository.buscarMasCocinadas(desde, hasta, PageRequest.of(0, 10));

        cocinadas.forEach(System.out::println);
        assertTrue(cocinadas.size() <= 10);
    }

    @Test
    void consultarRecetasMasGuardadas() {
        List<RecetaPopularResponse> guardadas =
                repository.buscarMasGuardadas(PageRequest.of(0, 10));

        guardadas.forEach(System.out::println);
        assertTrue(guardadas.size() <= 10);
    }
}