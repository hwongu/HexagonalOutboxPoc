package net.hwongu.poc.application.service;

import net.hwongu.poc.domain.model.Cliente;
import net.hwongu.poc.domain.port.out.ClienteRepository;

/**
 * <strong>Rol Orquestador del caso de uso de creacion de cliente</strong>
 *
 * <p>
 * Actuo como servicio de aplicacion que coordina el alta de un nuevo {@link Cliente}.
 * Recibo los datos de entrada, construyo el modelo de dominio y delego la persistencia
 * al puerto de salida {@link ClienteRepository}.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Separar la logica del caso de uso de los detalles de infraestructura.
 * Gracias a este servicio, la capa de entrada no necesita conocer como se almacena el cliente.
 * </p>
 *
 * <strong>Nota</strong> En esta POC el id se inicializa como null y la asignacion queda a cargo
 * de la implementacion del repositorio. La salida por consola es solo para trazabilidad basica.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public class CrearClienteService {

    private final ClienteRepository repositorio;

    public CrearClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void ejecutar(String nombre, Double saldoInicial) {
        Cliente nuevoCliente = new Cliente(null, nombre, saldoInicial);
        System.out.println("--- Iniciando proceso de alta de cliente ---");
        repositorio.guardar(nuevoCliente);
        System.out.println("--- Proceso finalizado ---");
    }
}
