package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.dtos.request.RegistroRequest;
import es.safareyes.smartchef.dtos.request.SemanaRequest;
import es.safareyes.smartchef.dtos.request.UsuarioRequest;
import es.safareyes.smartchef.dtos.response.RegistroResponse;
import es.safareyes.smartchef.dtos.response.SemanaResponse;
import es.safareyes.smartchef.service.ICocinaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Registros", description = "Historial de comidas del usuario")
@RestController
@RequestMapping("/api/v1/registros")
@AllArgsConstructor
public class CocinaController {

    private ICocinaService cocinaService;

    // POST - Registrar lo que ha cocinado el usuario (endpoint 14)
    @Operation(
            summary = "Registrar una comida",
            description = "Un registro por día y tipo de comida. No admite fechas futuras. "
                    + "Con descontarDespensa = true resta los ingredientes de la receta sin bajar de 0"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro creado"),
            @ApiResponse(responseCode = "400", description = "Datos no válidos o fecha futura"),
            @ApiResponse(responseCode = "404", description = "Usuario o receta no encontrados"),
            @ApiResponse(responseCode = "409", description = "Ya hay un registro de ese tipo ese día")
    })
    @PostMapping
    public ResponseEntity<RegistroResponse> registrar(
            @ParameterObject @Valid @ModelAttribute UsuarioRequest usuario,
            @Valid @RequestBody RegistroRequest datos) {

        RegistroResponse creado = cocinaService.registrar(usuario.idUsuario(), datos);

        URI ubicacion = URI.create("/api/v1/registros/" + creado.id());

        return ResponseEntity.created(ubicacion).body(creado);
    }

    // GET - Semana con comidas y calorías por día (endpoint 15)
    @Operation(
            summary = "Consultar la semana",
            description = "Devuelve los 7 días (lunes a domingo) de la semana de la fecha indicada, "
                    + "con las comidas registradas y las calorías de cada día"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de usuario no válido o fecha con formato incorrecto"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/semana")
    public ResponseEntity<SemanaResponse> obtenerSemana(
            @ParameterObject @Valid @ModelAttribute SemanaRequest datos) {

        return ResponseEntity.ok(
                cocinaService.obtenerSemana(datos.idUsuario(), datos.fecha())
        );
    }
}