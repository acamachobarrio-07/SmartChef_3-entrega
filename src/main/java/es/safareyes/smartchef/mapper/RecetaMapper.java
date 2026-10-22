
package es.safareyes.smartchef.mapper;

import es.safareyes.smartchef.dtos.DatosFiltradoReceta;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import es.safareyes.smartchef.entity.Receta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecetaMapper {

    @Mapping(target = "dificultad", source = "ERecetaDificultad")
    RecetaResponse aResponse(Receta receta);

    List<RecetaResponse> aResponseList(List<Receta> recetas);

    @Mapping(target = "dificultad", source = "ERecetaDificultad")
    RecetaResponse aResponseDesdeFiltro(DatosFiltradoReceta datos);

    List<RecetaResponse> aResponseDesdeFiltroList(
            List<DatosFiltradoReceta> datos
    );

}

