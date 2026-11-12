package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.dtos.request.BuscarPorIngredientesRequest;
import es.safareyes.smartchef.dtos.request.GuardarRecetaRequest;
import es.safareyes.smartchef.dtos.request.RecetasRequest;
import es.safareyes.smartchef.dtos.response.BuscarPorIngredientesResponse;
import es.safareyes.smartchef.dtos.response.RecetaDetalleResponse;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import es.safareyes.smartchef.service.IRecetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Recetas", description = "Catálogo de recetas de SmartChef")
@RestController
@RequestMapping("/api/v1/recetas")
@AllArgsConstructor
public class RecetaController {

    private IRecetaService recetaService;

    // GET - Consultar recetas con filtros
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

    // POST - Crear una receta
    @Operation(summary = "Crear una nueva receta con ingredientes y pasos")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Receta creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la receta no válidos",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se ha encontrado uno de los ingredientes"
            )
    })
    @PostMapping({"", "/crear"})
    public ResponseEntity<RecetaResponse> crearReceta(
            @Valid @RequestBody GuardarRecetaRequest json) {

        RecetaResponse recetaCreada = recetaService.crearReceta(json);

        URI ubicacion = URI.create(
                "/api/v1/recetas/" + recetaCreada.id()
        );

        return ResponseEntity
                .created(ubicacion)
                .body(recetaCreada);
    }

    // POST - Buscar recetas por ingredientes (RN-04)
    @Operation(
            summary = "Buscar recetas por ingredientes",
            description = "Recomienda recetas según los ingredientes disponibles, cumpliendo la RN-04"
    )
    @PostMapping("/buscar-por-ingredientes")
    public ResponseEntity<List<BuscarPorIngredientesResponse>> buscarPorIngredientes(
            @Valid @RequestBody BuscarPorIngredientesRequest datos) {

        return ResponseEntity.ok(recetaService.buscarPorIngredientes(datos));
    }

    // GET - Consultar el detalle de una receta
    @Operation(
            summary = "Obtener receta por ID",
            description = "Devuelve los datos de una receta con sus ingredientes, pasos y valores calculados"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receta encontrada"),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RecetaDetalleResponse> obtenerRecetaPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(recetaService.obtenerRecetaPorId(id));
    }


    // PUT - Modificar una receta existente
    @Operation(
            summary = "Modificar una receta existente",
            description = "Actualiza los datos, ingredientes y pasos de una receta"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Receta modificada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la receta no válidos",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Receta no encontrada"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<RecetaResponse> modificarReceta(
            @PathVariable Integer id,
            @Valid @RequestBody GuardarRecetaRequest dto) {

        return ResponseEntity.ok(
                recetaService.modificarReceta(id, dto)
        );
    }


    // Retirar una receta (baja lógica)
    @Operation(
            summary = "Retirar una receta",
            description = "Realiza una baja lógica sin eliminar la receta de la base de datos"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Receta retirada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Receta no encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La receta ya está retirada",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Void> retirarReceta(@PathVariable Integer id) {

        recetaService.retirarReceta(id);

        return ResponseEntity.noContent().build();
    }


}
