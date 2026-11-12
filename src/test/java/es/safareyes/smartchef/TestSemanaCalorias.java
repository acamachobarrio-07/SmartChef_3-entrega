package es.safareyes.smartchef;

import es.safareyes.smartchef.repository.ICocinaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import java.time.LocalDateTime;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestSemanaCalorias {

    @Autowired
    private ICocinaRepository repository;

    @Test
    void consultarResumenSemanal() {

        repository.obtenerResumenSemanal(
                2,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now()
        ).forEach(System.out::println);
    }
}