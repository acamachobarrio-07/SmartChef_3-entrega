package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.response.ColeccionResponse;

import java.util.List;

public interface IColeccionService {

    // Consultar las colecciones de un usuario con sus recetas
    List<ColeccionResponse> obtenerColecciones(Integer idUsuario);

}