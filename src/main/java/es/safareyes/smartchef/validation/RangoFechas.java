package es.safareyes.smartchef.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

// comprobamos que la fecha desde no sea posterior a hasta
@Documented
@Constraint(validatedBy = RangoFechasValidator.class)
@Target(ElementType.TYPE)            // se pone encima del record, no de un campo
@Retention(RetentionPolicy.RUNTIME)
public @interface RangoFechas {

    String message() default "La fecha 'desde' no puede ser posterior a la fecha 'hasta'";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}