package es.safareyes.smartchef.mapper;

import es.safareyes.smartchef.dtos.response.FilaColeccionResponse;
import es.safareyes.smartchef.dtos.response.RecetaColeccionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IColeccionMapper {

    // Convertir una fila de la consulta en la receta de una colección
    // tiempo, dificultad y retirada se llaman igual y se copian solos
    @Mapping(target = "id", source = "idReceta")
    @Mapping(target = "nombre", source = "receta")
    RecetaColeccionResponse aRecetaResponse(FilaColeccionResponse fila);

}