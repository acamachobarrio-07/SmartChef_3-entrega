package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.response.SemanaResponse;

import java.time.LocalDate;

public interface ICocinaService {

    // Semana (lunes a domingo) con las comidas y calorías de cada día
    SemanaResponse obtenerSemana(Integer idUsuario, LocalDate fecha);

}
