package es.safareyes.smartchef.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespensaResponse (

        Integer id,
        IngredienteResponse ingrediente,
        BigDecimal cantidad,
        LocalDate caducidad
        
){
}
