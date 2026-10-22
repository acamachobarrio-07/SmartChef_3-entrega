package es.safareyes.smartchef.dtos;

import es.safareyes.smartchef.entity.ERecetaDificultad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatosFiltradoReceta {

    private Integer id;
    private String nombre;
    private String descripcion;
    private Integer raciones;
    private Integer tiempo;
    private Integer calorias;
    private ERecetaDificultad eRecetaDificultad;

}
