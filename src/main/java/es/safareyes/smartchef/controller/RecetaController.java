package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.dtos.request.RecetasRequest;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import es.safareyes.smartchef.service.RecetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recetas", description = "Catálogo de recetas de SmartChef")
@RestController
@RequestMapping("/api/v1/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final RecetaService recetaService;

    @Operation(summary = "Buscar recetas con filtros y paginación")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de recetas obtenido correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Filtros no válidos",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<Page<RecetaResponse>> buscarRecetas(
            @Parameter(description = "Filtros opcionales para buscar recetas")
            @ParameterObject @Valid @ModelAttribute RecetasRequest filtros) {

        return ResponseEntity.ok(recetaService.buscarRecetas(filtros));
    }
}
