package es.safareyes.smartchef.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/*La marcamos como tabla*/
@Entity
@Table(name="despensa")
//lombok genera getters, setters, constructor y toString
@Getter
@Setter
@EqualsAndHashCode(exclude = {"usuario", "ingrediente"})
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"usuario", "ingrediente"})

public class Despensa {

    //atributos
    /*declaramos la id y tambien la ponemos como autoincrement*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="cantidad")
    private BigDecimal cantidad;

    @Column(name="caducidad")
    private LocalDate caducidad;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ingredientes", nullable = false)
    private Ingredientes ingrediente;
}
