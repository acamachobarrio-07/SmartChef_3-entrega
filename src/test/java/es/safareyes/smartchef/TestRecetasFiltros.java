package es.safareyes.smartchef;

import es.safareyes.smartchef.dtos.DatosFiltradoReceta;
import es.safareyes.smartchef.entity.ERecetaDificultad;
import es.safareyes.smartchef.repository.IRecetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import es.safareyes.smartchef.entity.ERecetaDificultad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestRecetasFiltros {

    @Autowired
    private IRecetaRepository repository;

    @Test
    void buscarporFiltros() {


        Page<DatosFiltradoReceta> resultados =
                repository.buscarConFiltros(
                        30,
                        ERecetaDificultad.FACIL,
                        true,
                        true,
                        null,
                        PageRequest.of(0, 10)
                );

    }

}
