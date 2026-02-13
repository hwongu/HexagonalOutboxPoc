package net.hwongu.poc.application.service;

import net.hwongu.poc.domain.model.Cliente;
import net.hwongu.poc.domain.port.out.ClienteRepository;

/**
 * <strong>Rol Orquestador del caso de uso de consulta de saldo</strong>
 *
 * <p>
 * Actuo como servicio de aplicacion que coordina la busqueda de un cliente mediante el puerto de salida
 * {@link ClienteRepository}. Luego presento el resultado en consola para la POC.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Mantener desacoplada la logica del caso de uso respecto a la tecnologia de persistencia
 * permitiendo cambiar implementaciones del repositorio sin afectar la capa de aplicacion.
 * </p>
 *
 * <strong>Nota</strong> Para un escenario real se recomienda reemplazar System.out.println por logging
 * y manejo de errores desde una capa de entrada o un handler de excepciones.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public class ConsultaSaldoService {

    private final ClienteRepository repositorio;

    public ConsultaSaldoService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Cliente ejecutarConsulta(Integer idCliente) {
        Cliente cliente = repositorio.buscarPorId(idCliente);
        if (cliente != null) {
            System.out.println("Consulta Exitosa:");
            System.out.println(cliente);
        } else {
            System.out.println("Error: Cliente no encontrado en la base de datos.");
        }
        return cliente;
    }

}
