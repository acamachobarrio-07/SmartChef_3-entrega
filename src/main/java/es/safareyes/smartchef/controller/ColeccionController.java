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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}