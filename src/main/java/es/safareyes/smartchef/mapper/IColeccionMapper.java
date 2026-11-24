package es.safareyes.smartchef.mapper;

import es.safareyes.smartchef.dtos.response.ColeccionResponse;
import es.safareyes.smartchef.dtos.response.FilaColeccionResponse;
import es.safareyes.smartchef.dtos.response.RecetaColeccionResponse;
import es.safareyes.smartchef.entity.Coleccion;
import es.safareyes.smartchef.entity.Receta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IColeccionMapper {

    // convertir una fila de la consulta en la receta de una colección
    @Mapping(target = "id", source = "idReceta")
    @Mapping(target = "nombre", source = "receta")
    RecetaColeccionResponse aRecetaResponse(FilaColeccionResponse fila);

    //convertir una receta en la receta de una colección

    @Mapping(target = "dificultad", source = "ERecetaDificultad")
    RecetaColeccionResponse recetaAResponse(Receta receta);

    //convertir la colección comlpeta
    //el Set<Receta> se convierte en List<RecetaColeccionResponse>
    //usando el método de arriba para cada receta

    ColeccionResponse aResponse(Coleccion coleccion);

}