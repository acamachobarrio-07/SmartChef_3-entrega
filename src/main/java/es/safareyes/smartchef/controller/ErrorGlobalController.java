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
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ErrorGlobalController {

    // 400 - Validaciones de los DTO (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail errorValidacion(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new LinkedHashMap<>();

        // Errores de cada campo (@NotNull, @Positive...)
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        // Errores de validadores de clase, como @RangoFechas
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errores.put(error.getObjectName(), error.getDefaultMessage())
        );

        ProblemDetail problema = construir(
                HttpStatus.BAD_REQUEST,
                "Datos no válidos",
                "Revisa los datos enviados"
        );
        problema.setProperty("errores", errores);

        return problema;
    }

    // 400 - Parámetro con formato incorrecto (por ejemplo /recetas/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail errorTipoParametro(MethodArgumentTypeMismatchException ex) {

        return construir(
                HttpStatus.BAD_REQUEST,
                "Parámetro no válido",
                "El parámetro '" + ex.getName() + "' tiene un formato incorrecto"
        );
    }

    // 400 - JSON mal formado: fechas con formato incorrecto, enums inexistentes...
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail jsonNoValido(HttpMessageNotReadableException ex) {

        return construir(
                HttpStatus.BAD_REQUEST,
                "JSON no válido",
                "Revisa el formato de los datos enviados: las fechas deben ser AAAA-MM-DD "
                        + "y los valores de los campos deben ser válidos"
        );
    }

    // 404 - El recurso buscado no existe
    @ExceptionHandler(RelacionNoEncontrada.class)
    public ProblemDetail relacionNoEncontrada(RelacionNoEncontrada ex) {

        return construir(
                HttpStatus.NOT_FOUND,
                "Recurso no encontrado",
                ex.getMessage()
        );
    }

    // 409 - Choca con algo que ya existe (nombre repetido, registro duplicado...)
    @ExceptionHandler(ElementoDuplicado.class)
    public ProblemDetail elementoDuplicado(ElementoDuplicado ex) {

        return construir(
                HttpStatus.CONFLICT,
                "Elemento duplicado",
                ex.getMessage()
        );
    }

    // 422 - La petición es correcta, pero una regla de negocio lo impide
    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaNegocio(ReglaNegocioException ex) {

        return construir(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Regla de negocio incumplida",
                ex.getMessage()
        );
    }

    // Método auxiliar: crea el ProblemDetail con el mismo formato para todos
    private ProblemDetail construir(HttpStatus estado, String titulo, String detalle) {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        problema.setProperty("timestamp", LocalDateTime.now());

        return problema;
    }
}