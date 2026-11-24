package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.response.ColeccionResponse;

import java.util.List;

public interface IColeccionService {

    // consultar las colecciones de un usuario con sus recetas
    List<ColeccionResponse> obtenerColecciones(Integer idUsuario);

    //guardar receta en coleccion, creándola sino existe

    ColeccionResponse guardarReceta(Integer idUsuario, String nombreColeccion, Integer idReceta);

}