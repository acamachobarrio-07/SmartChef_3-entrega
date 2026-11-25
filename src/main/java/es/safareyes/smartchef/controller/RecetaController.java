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

    // GET - Consultar recetas con filtros (endpoint 1)
    @Operation(
            summary = "Buscar recetas con filtros y paginación",
            description = "Las recetas retiradas no aparecen (RN-09)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de recetas obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "Filtros no válidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<Page<RecetaResponse>> buscarRecetas(
            @Parameter(description = "Filtros opcionales para buscar recetas")
            @ParameterObject @Valid @ModelAttribute RecetasRequest filtros) {

        return ResponseEntity.ok(recetaService.buscarRecetas(filtros));
    }

    // POST - Crear una receta (endpoint 3)
    @Operation(summary = "Crear una nueva receta con ingredientes y pasos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Receta creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de la receta no válidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No se ha encontrado uno de los ingredientes",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe una receta con ese nombre",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Pasos con huecos o ingredientes repetidos (RN-01)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<RecetaResponse> crearReceta(
            @Valid @RequestBody GuardarRecetaRequest json) {

        RecetaResponse recetaCreada = recetaService.crearReceta(json);

        URI ubicacion = URI.create("/api/v1/recetas/" + recetaCreada.id());

        return ResponseEntity.created(ubicacion).body(recetaCreada);
    }

    // POST - Buscar recetas por ingredientes (endpoint 7, RN-04)
    @Operation(
            summary = "Buscar recetas por ingredientes",
            description = "Recibe una lista de ingredientes O un usuario (usa su despensa sin caducados). "
                    + "Devuelve recetas con más del 50 % de coincidencia (RN-04)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recetas recomendadas ordenadas por coincidencia"),
            @ApiResponse(responseCode = "400", description = "Datos no válidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Hay que indicar ingredientes o usuario, no ambos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/buscar-por-ingredientes")
    public ResponseEntity<List<BuscarPorIngredientesResponse>> buscarPorIngredientes(
            @Valid @RequestBody BuscarPorIngredientesRequest datos) {

        return ResponseEntity.ok(recetaService.buscarPorIngredientes(datos));
    }

    // GET - Consultar el detalle de una receta (endpoint 2)
    @Operation(
            summary = "Obtener receta por ID",
            description = "Devuelve los datos de una receta con sus ingredientes, pasos y valores calculados (RN-02)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receta encontrada"),
            @ApiResponse(responseCode = "400", description = "ID con formato incorrecto",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RecetaDetalleResponse> obtenerRecetaPorId(@PathVariable Integer id) {

        return ResponseEntity.ok(recetaService.obtenerRecetaPorId(id));
    }

    // PUT - Modificar una receta existente (endpoint 4)
    @Operation(
            summary = "Modificar una receta existente",
            description = "Actualiza los datos, ingredientes y pasos de una receta"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receta modificada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de la receta no válidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Receta o ingrediente no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe otra receta con ese nombre",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Pasos con huecos o ingredientes repetidos (RN-01)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<RecetaResponse> modificarReceta(
            @PathVariable Integer id,
            @Valid @RequestBody GuardarRecetaRequest dto) {

        return ResponseEntity.ok(recetaService.modificarReceta(id, dto));
    }

    // PATCH - Retirar una receta, baja lógica (endpoint 5)
    @Operation(
            summary = "Retirar una receta",
            description = "Baja lógica: la receta deja de salir en búsquedas pero sigue en colecciones e historial (RN-09)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Receta retirada correctamente"),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "La receta ya está retirada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Void> retirarReceta(@PathVariable Integer id) {

        recetaService.retirarReceta(id);

        return ResponseEntity.noContent().build();
    }
}
