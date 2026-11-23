package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.dtos.request.SemanaRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Registros", description = "Historial de comidas del usuario")
@RestController
@RequestMapping("/api/v1/registros")
@AllArgsConstructor

public class CocinaController {

    private ICocinaService cocinaService;

    // GET - Semana con comidas y calorías por día
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