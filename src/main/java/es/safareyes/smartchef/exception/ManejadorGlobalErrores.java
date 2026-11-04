package es.safareyes.smartchef.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorGlobalErrores {

    // Error 400 - Validaciones de los DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail errorValidacion(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(
                        error.getField(),
                        error.getDefaultMessage()
                )
        );

        ProblemDetail problema =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problema.setTitle("Datos no válidos");
        problema.setDetail("Revisa los datos enviados");
        problema.setProperty("errores", errores);

        return problema;
    }

    // Error 409 - Receta con nombre duplicado
    @ExceptionHandler(ElementoDuplicado.class)
    public ProblemDetail elementoDuplicado(ElementoDuplicado ex) {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problema.setTitle("Elemento duplicado");

        return problema;
    }

    // Error 404 - Ingrediente no encontrado
    @ExceptionHandler(RelacionNoEncontrada.class)
    public ProblemDetail relacionNoEncontrada(RelacionNoEncontrada ex) {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );

        problema.setTitle("Relación no encontrada");

        return problema;
    }

    // Error 400 - Regla de negocio incumplida
    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaNegocio(ReglaNegocioException ex) {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problema.setTitle("Regla de negocio incumplida");

        return problema;
    }
}

