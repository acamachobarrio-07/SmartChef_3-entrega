package es.safareyes.smartchef.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lista_compra")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"usuario", "ingrediente"})
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"usuario", "ingrediente"})
public class ListaCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EEstadoLista estado = EEstadoLista.ABIERTA;

    @Column(name = "comprado", nullable = false)
    private Boolean comprado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ingredientes", nullable = false)
    private Ingredientes ingrediente;
}
