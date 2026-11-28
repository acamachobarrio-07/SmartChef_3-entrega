package es.safareyes.smartchef.mapper;

import es.safareyes.smartchef.dtos.request.GuardarRecetaRequest;
import es.safareyes.smartchef.dtos.response.IngredienteRecetaResponse;
import es.safareyes.smartchef.dtos.response.PasoRecetaResponse;
import es.safareyes.smartchef.dtos.response.RecetaResponse;
import es.safareyes.smartchef.entity.IngredientesReceta;
import es.safareyes.smartchef.entity.Paso;
import es.safareyes.smartchef.entity.Receta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IRecetaMapper {

    // convertir entidad a DTO de respuesta
    @Mapping(target = "dificultad", source = "ERecetaDificultad")
    RecetaResponse aResponse(Receta receta);

    // convertir DTO de creación a entidad
    // retirada se ignora: una receta nueva nunca nace retirada (vale false por defecto)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "calorias", ignore = true)
    @Mapping(target = "retirada", ignore = true)
    @Mapping(target = "ERecetaDificultad", source = "dificultad")
    @Mapping(target = "ingredientesReceta", ignore = true)
    @Mapping(target = "pasos", ignore = true)
    @Mapping(target = "fotos", ignore = true)
    @Mapping(target = "colecciones", ignore = true)
    Receta toEntity(GuardarRecetaRequest dto);

    // convertir los ingredientes de una receta a DTO
    @Mapping(target = "idIngrediente", source = "ingrediente.id")
    @Mapping(target = "nombre", source = "ingrediente.nombre")
    @Mapping(target = "tipoCantidad", source = "ingrediente.EIngredientesTipoCantidad")
    IngredienteRecetaResponse toIngredienteDto(IngredientesReceta ingredienteReceta);

    List<IngredienteRecetaResponse> toIngredientesDto(List<IngredientesReceta> ingredientes);

    // convertir los pasos de una receta a DTO
    PasoRecetaResponse toPasoDto(Paso paso);

    List<PasoRecetaResponse> toPasosDto(List<Paso> pasos);

    // actualizar una receta existente
    // retirada se ignora: el PUT no puede retirar ni reactivar (eso es el PATCH)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "calorias", ignore = true)
    @Mapping(target = "retirada", ignore = true)
    @Mapping(target = "ERecetaDificultad", source = "dificultad")
    @Mapping(target = "ingredientesReceta", ignore = true)
    @Mapping(target = "pasos", ignore = true)
    @Mapping(target = "fotos", ignore = true)
    @Mapping(target = "colecciones", ignore = true)
    void actualizarReceta(GuardarRecetaRequest dto, @MappingTarget Receta receta);
}