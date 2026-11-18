package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.dtos.request.ActualizarDespensaRequest;
import es.safareyes.smartchef.dtos.request.DespensaRequest;
import es.safareyes.smartchef.dtos.response.DespensaResponse;
import es.safareyes.smartchef.service.IDespensaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Despensa", description = "Gestión de la despensa de SmartChef")
@RestController
@RequestMapping("/api/v1/despensa")
@AllArgsConstructor
public class DespensaController {

    private IDespensaService despensaService;

    // GET - Consultar la despensa
    @Operation(
            summary = "Consultar despensa",
            description = "Devuelve los ingredientes de la despensa de un usuario ordenados por fecha de caducidad"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de usuario no válido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping
    public ResponseEntity<List<DespensaResponse>> obtenerDespensa(
            @ParameterObject @Valid @ModelAttribute DespensaRequest datos) {

        return ResponseEntity.ok(despensaService.obtenerDespensa(datos));
    }

    // PUT - Añadir o actualizar un ingrediente (cantidad 0 lo elimina)
    @Operation(
            summary = "Añadir o actualizar un ingrediente de la despensa",
            description = "Guarda la cantidad y la caducidad del ingrediente. Si la cantidad es 0, se elimina de la despensa"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingrediente añadido o actualizado"),
            @ApiResponse(responseCode = "204", description = "Ingrediente eliminado de la despensa (cantidad 0)"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos no válidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuario o ingrediente no encontrado")
    })
    @PutMapping("/{ingredienteId}")
    public ResponseEntity<DespensaResponse> guardarEnDespensa(
            @PathVariable Integer ingredienteId,
            @ParameterObject @Valid @ModelAttribute DespensaRequest usuario,
            @Valid @RequestBody ActualizarDespensaRequest datos) {

        DespensaResponse resultado = despensaService.guardarEnDespensa(
                usuario.idUsuario(),
                ingredienteId,
                datos
        );

        // Si el servicio devuelve null, el ingrediente se ha eliminado
        if (resultado == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(resultado);
    }
}