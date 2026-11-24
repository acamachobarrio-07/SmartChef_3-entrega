package es.safareyes.smartchef.dtos.request;

import es.safareyes.smartchef.validation.RangoFechas;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@RangoFechas
public record InformeRequest(

        @Schema(description = "Fecha inicial (incluida)", example = "2026-11-01")
        @NotNull(message = "La fecha 'desde' es obligatoria")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate desde,

        @Schema(description = "Fecha final (incluida)", example = "2026-11-30")
        @NotNull(message = "La fecha 'hasta' es obligatoria")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate hasta
) {
}