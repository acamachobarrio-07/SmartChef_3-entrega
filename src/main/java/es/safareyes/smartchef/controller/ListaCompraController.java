package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.dtos.request.DesdeRecetaRequest;
import es.safareyes.smartchef.dtos.response.ListaCompraResponse;
import es.safareyes.smartchef.service.IListaCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import es.safareyes.smartchef.dtos.request.UsuarioRequest;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Lista de compra", description = "Gestión de la lista de la compra")
@RestController
@RequestMapping("/api/v1/listas-compra")
@AllArgsConstructor
public class ListaCompraController {

    private IListaCompraService listaCompraService;

    // POST - Generar lista de la compra desde una receta
    @Operation(
            summary = "Generar lista de la compra",
            description = "Genera la lista de la compra teniendo en cuenta las raciones y los ingredientes disponibles en la despensa"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de la compra generada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos no válidos o ID de usuario incorrecto"),
            @ApiResponse(responseCode = "404", description = "Usuario o receta no encontrados"),
            @ApiResponse(responseCode = "422", description = "La receta está retirada")
    })
    @PostMapping("/desde-receta")
    public ResponseEntity<ListaCompraResponse> generarDesdeReceta(
            @ParameterObject @Valid @ModelAttribute UsuarioRequest usuario,
            @Valid @RequestBody DesdeRecetaRequest request) {

        return ResponseEntity.ok(
                listaCompraService.generarDesdeReceta(usuario.idUsuario(), request)
        );
    }
}
