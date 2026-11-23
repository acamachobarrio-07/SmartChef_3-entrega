package es.safareyes.smartchef.service.impl;

import es.safareyes.smartchef.dtos.request.CerrarListaRequest;
import es.safareyes.smartchef.dtos.request.DesdeRecetaRequest;
import es.safareyes.smartchef.dtos.response.ListaCompraIngredienteResponse;
import es.safareyes.smartchef.dtos.response.ListaCompraResponse;
import es.safareyes.smartchef.entity.*;
import es.safareyes.smartchef.exception.ReglaNegocioException;
import es.safareyes.smartchef.exception.RelacionNoEncontrada;
import es.safareyes.smartchef.mapper.IListaCompraMapper;
import es.safareyes.smartchef.repository.IDespensaRepository;
import es.safareyes.smartchef.repository.IListaCompraRepository;
import es.safareyes.smartchef.repository.IRecetaRepository;
import es.safareyes.smartchef.repository.IUsuarioRepository;
import es.safareyes.smartchef.service.IListaCompraService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ListaCompraServiceImpl implements IListaCompraService {

    private IListaCompraRepository listaCompraRepository;
    private IUsuarioRepository usuarioRepository;
    private IRecetaRepository recetaRepository;
    private IDespensaRepository despensaRepository;
    private IListaCompraMapper listaCompraMapper;

    //generar lista de la compra desde una receta
    @Override
    @Transactional
    public ListaCompraResponse generarDesdeReceta(Integer idUsuario, DesdeRecetaRequest request) {

        //primero comprobamos que existe el usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe el usuario con ID: " + idUsuario
                ));

        //comprobamos si existe la receta
        Receta receta = recetaRepository.findById(request.idReceta())
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe la receta con ID: " + request.idReceta()
                ));

        //comprobar que la receta no esta retirada
        if (Boolean.TRUE.equals(receta.getRetirada())) {
            throw new ReglaNegocioException(
                    "No se puede generar una lista de una receta retirada");
        }

        //Obtener los ingredientes de una receta
        List<IngredientesReceta> ingredientesReceta =
                receta.getIngredientesReceta();

        //obtener los ingredientes disponibles en la despensa
        List<Despensa> despensa = despensaRepository.buscarIngredientesDisponibles(
                idUsuario,
                LocalDate.now()
        );

        //guardamos las cantidades disponibles por ingrediente
        Map<Integer, BigDecimal> cantidadesDespensa = new HashMap<>();

        for (Despensa d : despensa) {

            Integer idIngrediente = d.getIngrediente().getId();

            cantidadesDespensa.merge(
                    idIngrediente,
                    d.getCantidad(),
                    BigDecimal::add
            );
        }

        //recorremos todos los ingredientes de la receta
        for (IngredientesReceta i : ingredientesReceta) {

            Ingredientes ingrediente = i.getIngrediente();

            Integer idIngrediente = ingrediente.getId();

            //calculamos la cantidad necesaria según las raciones
            BigDecimal necesaria = i.getCantidad()
                    .multiply(BigDecimal.valueOf(request.raciones()))
                    .divide(
                            BigDecimal.valueOf(receta.getRaciones()),
                            2,
                            RoundingMode.HALF_UP
                    );

            //Consultar la cantidad disponible
            BigDecimal disponible = cantidadesDespensa.getOrDefault(
                    idIngrediente,
                    BigDecimal.ZERO
            );

            //Calcular la cantidad que falta
            BigDecimal falta = necesaria.subtract(disponible);

            //Si tenemos suficiente, no añadimos el ingrediente
            if (falta.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            //comprobamos si ya existe una lista abierta
            Optional<ListaCompra> existente =
                    listaCompraRepository.findByUsuarioIdAndIngredienteIdAndEstado(
                            idUsuario,
                            idIngrediente,
                            EEstadoLista.ABIERTA
                    );

            ListaCompra listaCompra;

            if (existente.isPresent()) {

                // Si existe, sumamos la cantidad que falta
                listaCompra = existente.get();

                listaCompra.setCantidad(
                        listaCompra.getCantidad().add(falta)
                );
            } else {

                //no existe, creamos una nueva fila
                listaCompra = new ListaCompra();

                listaCompra.setUsuario(usuario);
                listaCompra.setIngrediente(ingrediente);
                listaCompra.setCantidad(falta);
                listaCompra.setFecha(LocalDateTime.now());
                listaCompra.setEstado(EEstadoLista.ABIERTA);
                listaCompra.setComprado(false);

            }

            //guardo el ingrediente
            listaCompraRepository.save(listaCompra);

        }

        //obtenemos todas las filas abiertas del usuario
        List<ListaCompra> listaAbierta =
                listaCompraRepository.findByUsuarioIdAndEstadoOrderByFechaAsc(
                        idUsuario,
                        EEstadoLista.ABIERTA
                );

        //convertir las entidades a DTOS
        List<ListaCompraIngredienteResponse> ingredientes =
                listaCompraMapper.toDtoList(listaAbierta);

        //devolver lista abierta
        return new ListaCompraResponse(
                idUsuario,
                EEstadoLista.ABIERTA,
                ingredientes
        );
    }

    //cerrar lista abierta y pasar lo comprado a la despensa
    @Override
    @Transactional
    public ListaCompraResponse cerrarLista(Integer idUsuario, CerrarListaRequest request) {
        //comprobar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RelacionNoEncontrada(
                        "No existe el usuario con ID: " + idUsuario
                ));

        //obtener las listas abiertas del usuario por fila

        List<ListaCompra> filas = listaCompraRepository.findByUsuarioIdAndEstadoOrderByFechaAsc(
                idUsuario,
                EEstadoLista.ABIERTA
        );

        //si no tiene ninguna, no tenemos nada que cerrar

        if(filas.isEmpty()){
            throw new ReglaNegocioException(
                    "No tienes ninguna lista de la compra abierta"
            );
        }

        //id recibidos son filas abiertas suyas

        for (Integer idComprado : request.idsComprados()) {

            boolean esSuya = filas.stream()
                    .anyMatch(fila -> fila.getId().equals(idComprado));

            if (!esSuya) {
                throw new RelacionNoEncontrada(
                        "La fila " + idComprado + " no está en tu lista de la compra abierta"
                );
            }
        }

        // bucle para recorrer las filas:
        // marcamos las compras y las sumamos a la despensa
        for (ListaCompra fila : filas) {

            // Si esta fila no se ha comprado, pasamos a la siguiente
            if (!request.idsComprados().contains(fila.getId())) {
                continue;
            }

            fila.setComprado(true);

            // buscamos el ingrediente en la despensa
            // sino lo tenemos creamos una nueva
            Despensa despensa = despensaRepository
                    .findByUsuarioIdAndIngredienteId(idUsuario, fila.getIngrediente().getId())
                    .orElseGet(() -> {
                        Despensa nueva = new Despensa();
                        nueva.setUsuario(usuario);
                        nueva.setIngrediente(fila.getIngrediente());
                        nueva.setCantidad(BigDecimal.ZERO);
                        return nueva;
                    });

            // sumamos lo comprado a lo que ya teniamos
            despensa.setCantidad(despensa.getCantidad().add(fila.getCantidad()));
            despensaRepository.save(despensa);
        }

        // cerramos todas las filas abiertas
        for (ListaCompra fila : filas) {
            fila.setEstado(EEstadoLista.CERRADA);
        }

        //devolvemos las listas cerradas
        return new ListaCompraResponse(
                idUsuario,
                EEstadoLista.CERRADA,
                listaCompraMapper.toDtoList(filas)
        );

    }
}
