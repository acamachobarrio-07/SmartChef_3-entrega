package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.dtos.request.DespensaRequest;
import es.safareyes.smartchef.dtos.response.DespensaResponse;
import es.safareyes.smartchef.service.IDespensaService;
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

import java.util.List;

@Tag(name = "Despensa", description = "Gestión de la despensa de SmartChef")
@RestController
@RequestMapping("/api/v1/despensa")
@AllArgsConstructor
public class DespensaController {

    private IDespensaService despensaService;

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
}