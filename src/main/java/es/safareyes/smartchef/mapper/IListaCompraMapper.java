package es.safareyes.smartchef.mapper;

import es.safareyes.smartchef.dtos.response.ListaCompraIngredienteResponse;
import es.safareyes.smartchef.entity.ListaCompra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IListaCompraMapper {

    @Mapping(target = "idIngrediente", source = "ingrediente.id")
    @Mapping(target = "nombre", source = "ingrediente.nombre")
    @Mapping(target = "tipoCantidad", source = "ingrediente.EIngredientesTipoCantidad")    ListaCompraIngredienteResponse toDto(ListaCompra listaCompra);

    List<ListaCompraIngredienteResponse> toDtoList(List<ListaCompra> listaCompra);
}

