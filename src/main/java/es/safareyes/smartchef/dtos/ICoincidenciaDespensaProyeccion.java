package es.safareyes.smartchef.dtos;

import java.math.BigDecimal;

// Resultado de la consulta nativa de coincidencia con la despensa (endpoint 7).
// Cada getter recibe la columna de la consulta con el mismo alias.
public interface ICoincidenciaDespensaProyeccion {

    Integer getIdReceta();

    Long getTotal();            // ingredientes que tiene la receta

    Long getCoincidentes();     // de ellos, cuántos hay en la despensa

    BigDecimal getPorcentaje();
}