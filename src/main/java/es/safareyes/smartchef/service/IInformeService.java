package es.safareyes.smartchef.service;

import es.safareyes.smartchef.dtos.response.InformeRecetasResponse;

import java.time.LocalDate;

public interface IInformeService {

    //recetas mas cocinadas entre dos fechas

    InformeRecetasResponse recetasPopulares(LocalDate desde, LocalDate hasta);

}
