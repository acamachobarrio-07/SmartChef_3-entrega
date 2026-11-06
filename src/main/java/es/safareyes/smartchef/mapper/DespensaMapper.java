package es.safareyes.smartchef.mapper;

import es.safareyes.smartchef.dtos.response.DespensaResponse;
import es.safareyes.smartchef.entity.Despensa;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = IngredienteMapper.class)
public interface DespensaMapper {

    // Convertir una entidad Despensa a su DTO
    DespensaResponse toDto(Despensa despensa);

    // Convertir una lista de despensas a DTOs
    List<DespensaResponse> toDtoList(List<Despensa> despensas);

}
