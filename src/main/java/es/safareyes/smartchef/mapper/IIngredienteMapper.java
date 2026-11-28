package es.safareyes.smartchef.mapper;

import es.safareyes.smartchef.dtos.response.IngredienteResponse;
import es.safareyes.smartchef.entity.Ingredientes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")

public interface IIngredienteMapper {

    // convertir entidad a DTO de respuesta
    @Mapping(target = "tipoCantidad",
            source = "EIngredientesTipoCantidad")
    IngredienteResponse toDto(Ingredientes ingrediente);

    // convertir una lista de entidades a DTO
    List<IngredienteResponse> toDtoList(
            List<Ingredientes> ingredientes
    );

}
