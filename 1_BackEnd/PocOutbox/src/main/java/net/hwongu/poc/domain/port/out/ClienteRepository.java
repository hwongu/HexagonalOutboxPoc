package net.hwongu.poc.domain.port.out;

import net.hwongu.poc.domain.model.Cliente;

/**
 * <strong>Rol Puerto de salida para persistencia y consulta de clientes</strong>
 *
 * <p>
 * Actuo como contrato del dominio hacia el exterior para almacenar y recuperar instancias de
 * {@link Cliente}. El dominio define aqui lo que necesita, sin depender de una tecnologia especifica.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Permitir multiples implementaciones de persistencia en adaptadores de infraestructura
 * por ejemplo base de datos legacy base de datos cloud memoria o mocks de prueba
 * manteniendo estable la capa de dominio.
 * </p>
 *
 * <strong>Nota</strong> El comportamiento ante ausencia de datos depende de la implementacion
 * por ejemplo retornar null o lanzar una excepcion controlada.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public interface ClienteRepository {

    void guardar(Cliente cliente);

    Cliente buscarPorId(Integer id);

}