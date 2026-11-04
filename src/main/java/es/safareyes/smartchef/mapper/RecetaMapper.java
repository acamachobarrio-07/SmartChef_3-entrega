package es.safareyes.smartchef.mapper;

import es.safareyes.smartchef.dtos.DatosFiltradoReceta;
import es.safareyes.smartchef.dtos.request.CrearRecetaRequest;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import es.safareyes.smartchef.entity.Receta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecetaMapper {

    // Convertir entidad a DTO de respuesta
    @Mapping(target = "dificultad", source = "ERecetaDificultad")
    RecetaResponse aResponse(Receta receta);

    List<RecetaResponse> aResponseList(List<Receta> recetas);

    // Convertir resultados del GET a DTO
    @Mapping(target = "dificultad", source = "ERecetaDificultad")
    RecetaResponse aResponseDesdeFiltro(DatosFiltradoReceta datos);

    List<RecetaResponse> aResponseDesdeFiltroList(
            List<DatosFiltradoReceta> datos
    );

    // Convertir DTO de creación a entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "calorias", ignore = true)
    @Mapping(target = "ERecetaDificultad", source = "dificultad")
    @Mapping(target = "ingredientesReceta", ignore = true)
    @Mapping(target = "pasos", ignore = true)
    @Mapping(target = "fotos", ignore = true)
    @Mapping(target = "colecciones", ignore = true)
    Receta toEntity(CrearRecetaRequest dto);

}

