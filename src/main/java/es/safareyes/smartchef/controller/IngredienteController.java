package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.dtos.request.IngredienteRequest;
import es.safareyes.smartchef.dtos.response.IngredienteResponse;
import es.safareyes.smartchef.service.IIngredienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ingredientes", description = "Catálogo de ingredientes de SmartChef")
@RestController
@RequestMapping("/api/v1/ingredientes")
@AllArgsConstructor
public class IngredienteController {

    private IIngredienteService ingredienteService;

    @Operation(
            summary = "Buscar ingredientes",
            description = "Devuelve un catálogo paginado de ingredientes, con búsqueda opcional por nombre"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda no válidos")
    })
    @GetMapping
    public ResponseEntity<Page<IngredienteResponse>> buscarIngredientes(
            @Parameter(description = "Filtros y paginación del catálogo")
            @ParameterObject @Valid @ModelAttribute IngredienteRequest filtros) {

        return ResponseEntity.ok(ingredienteService.buscarIngredientes(filtros));
    }
}