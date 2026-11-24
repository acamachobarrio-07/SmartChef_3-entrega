package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.request.RegistroRequest;
import es.safareyes.smartchef.dtos.response.RegistroResponse;
import es.safareyes.smartchef.dtos.response.SemanaResponse;

import java.time.LocalDate;

public interface ICocinaService {

    // Semana (lunes a domingo) con las comidas y calorías de cada día
    SemanaResponse obtenerSemana(Integer idUsuario, LocalDate fecha);

    // Registrar lo que ha cocinado el usuario (RN-07)
    RegistroResponse registrar(Integer idUsuario, RegistroRequest datos);

}
