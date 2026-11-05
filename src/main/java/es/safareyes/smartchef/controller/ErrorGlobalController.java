package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.exception.ElementoDuplicado;
import es.safareyes.smartchef.exception.ReglaNegocioException;
import es.safareyes.smartchef.exception.RelacionNoEncontrada;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ErrorGlobalController {

    // Error 400 - Validaciones de los DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail errorValidacion(MethodArgumentNotValidException ex) {

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

    // 400 - Error parámetro no valido
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail errorTipoParametro(
            MethodArgumentTypeMismatchException ex) {

        ProblemDetail problema =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problema.setTitle("Parámetro no válido");

        problema.setDetail(
                "El parámetro '" + ex.getName()
                        + "' tiene un formato incorrecto"
        );

        return problema;
    }

}

