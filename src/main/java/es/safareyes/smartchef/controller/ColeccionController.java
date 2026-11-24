package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.dtos.request.UsuarioRequest;
import es.safareyes.smartchef.dtos.response.ColeccionResponse;
import es.safareyes.smartchef.service.IColeccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Colecciones", description = "Colecciones de recetas de cada usuario")
@RestController
@RequestMapping("/api/v1/colecciones")
@AllArgsConstructor
public class ColeccionController {

    private IColeccionService coleccionService;

    // GET - Mis colecciones con sus recetas
    @Operation(
            summary = "Consultar mis colecciones",
            description = "Devuelve las colecciones del usuario con sus recetas, incluidas las colecciones vacías. "
                    + "Las recetas retiradas aparecen marcadas con retirada = true"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de usuario no válido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping
    public ResponseEntity<List<ColeccionResponse>> obtenerColecciones(
            @ParameterObject @Valid @ModelAttribute UsuarioRequest usuario) {

        return ResponseEntity.ok(
                coleccionService.obtenerColecciones(usuario.idUsuario())
        );
    }

    // PUT - Guardar receta en colección, creándola si no existe (RN-08)
    @Operation(
            summary = "Guardar receta en una colección",
            description = "Añade la receta a la colección del usuario con ese nombre (sin distinguir mayúsculas). "
                    + "Si el usuario no tiene ninguna colección con ese nombre, se crea"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receta guardada en la colección"),
            @ApiResponse(responseCode = "400", description = "ID de usuario o de receta no válido"),
            @ApiResponse(responseCode = "404", description = "Usuario o receta no encontrados"),
            @ApiResponse(responseCode = "409", description = "La receta ya está en esa colección"),
            @ApiResponse(responseCode = "422", description = "La receta está retirada y no se puede guardar")
    })
    @PutMapping("/{nombre}/recetas/{recetaId}")
    public ResponseEntity<ColeccionResponse> guardarReceta(
            @PathVariable String nombre,
            @PathVariable Integer recetaId,
            @ParameterObject @Valid @ModelAttribute UsuarioRequest usuario) {

        return ResponseEntity.ok(
                coleccionService.guardarReceta(usuario.idUsuario(), nombre, recetaId)
        );
    }
}