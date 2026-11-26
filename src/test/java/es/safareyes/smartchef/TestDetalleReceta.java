package es.safareyes.smartchef;

import es.safareyes.smartchef.entity.Receta;
import es.safareyes.smartchef.repository.IRecetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

// Endpoint 2 - GET /api/v1/recetas/{id} (detalle con ingredientes y pasos)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestDetalleReceta {

    @Autowired
    private IRecetaRepository repository;

    @Test
    void obtenerDetalleReceta() {
        // @DataJpaTest abre una transacción, así que las listas LAZY se pueden recorrer
        Receta receta = repository.findById(2).orElseThrow();

        System.out.println(receta.getNombre() + " (" + receta.getRaciones() + " raciones)");
        receta.getIngredientesReceta().forEach(ir ->
                System.out.println("  - " + ir.getIngrediente().getNombre() + ": " + ir.getCantidad()));
        receta.getPasos().forEach(p ->
                System.out.println("  " + p.getNumero() + ". " + p.getDescripcion()));
    }
}