package es.safareyes.smartchef.validation;

import jakarta.validation.ConstraintValidatorContext;
import es.safareyes.smartchef.dtos.request.InformeRequest;
import jakarta.validation.ConstraintValidator;

public class RangoFechasValidator implements ConstraintValidator<RangoFechas, InformeRequest> {

    @Override
    public boolean isValid(InformeRequest valor, ConstraintValidatorContext contexto) {

        // Si falta alguna fecha, de eso se encarga @NotNull
        if (valor == null || valor.desde() == null || valor.hasta() == null) {
            return true;
        }

        // Válido si 'desde' es anterior o igual a 'hasta'
        return !valor.desde().isAfter(valor.hasta());
    }
}
