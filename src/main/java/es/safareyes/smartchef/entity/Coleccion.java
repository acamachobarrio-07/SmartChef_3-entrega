package es.safareyes.smartchef.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/*La marcamos como tabla*/
@Entity
//en la tabla colección no se puede repetir la pareja id_usuario+nombre
// por ejemplo usuario 2 y 3 pueden tener colección Favoritas
// pero el usuario 3 no puede tener dos colecciones favoritas
@Table(name = "coleccion", uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "nombre"}))
//lombok genera getters, setters, constructor y toString
@Getter
@Setter
@EqualsAndHashCode(exclude = {"usuario", "recetas"})
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"usuario", "recetas"})
public class Coleccion {

    //atributos
    /*declaramos la id y tambien la ponemos como autoincrement*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="nombre", length = 150, nullable = false)
    private String nombre;

    /*para indicar el nombre de las relaciones hay que indicar un objeto dentro de otro*/
    /*hay que ponerle el tipo de relación, se pone en ingles, luego hay que añadir por donde une*/
    /*usamos lazy para cuando el usuario consulte el dato concreto*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_usuario")
    private Usuario usuario;

    //N:M coleccion_receta
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "coleccion_receta",
            joinColumns = @JoinColumn(name = "id_coleccion"),
            inverseJoinColumns = @JoinColumn(name = "id_receta")
    )
    private Set<Receta> recetas = new HashSet<>();

}
