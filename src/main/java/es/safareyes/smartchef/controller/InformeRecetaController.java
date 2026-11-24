package es.safareyes.smartchef.controller;

import es.safareyes.smartchef.dtos.request.InformeRequest;
import es.safareyes.smartchef.dtos.response.InformeRecetasResponse;
import es.safareyes.smartchef.service.IInformeService;
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

@Tag(name = "Informes", description = "Informes para el equipo de SmartChef")
@RestController
@RequestMapping("/api/v1/informes")
@AllArgsConstructor
public class InformeRecetaController {

    private IInformeService informeService;

    // GET - Top recetas más cocinadas y más guardadas
    @Operation(
            summary = "Top recetas más cocinadas y más guardadas",
            description = "Devuelve las recetas más cocinadas entre dos fechas y las más guardadas en colecciones"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Informe generado correctamente"),
            @ApiResponse(responseCode = "400", description = "Faltan las fechas, formato incorrecto o 'desde' posterior a 'hasta'")
    })
    @GetMapping("/recetas-populares")
    public ResponseEntity<InformeRecetasResponse> recetasPopulares(
            @ParameterObject @Valid @ModelAttribute InformeRequest fechas) {

        return ResponseEntity.ok(
                informeService.recetasPopulares(fechas.desde(), fechas.hasta())
        );
    }
}