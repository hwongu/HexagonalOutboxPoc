package net.hwongu.poc.worker.domain.port;

import net.hwongu.poc.worker.domain.model.OutboxEvent;
import java.util.List;

/**
 * <strong>Rol Puerto de salida para lectura y marcado de eventos Outbox</strong>
 *
 * <p>
 * Actuo como contrato del dominio del worker hacia la tabla outbox en la base Cloud.
 * Permito consultar eventos pendientes y marcar un evento como procesado luego de sincronizarlo
 * hacia Legacy.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Aislar el acceso a datos (SQL, JDBC, driver, etc) de la logica de sincronizacion.
 * El servicio de aplicacion solo conoce este puerto y no depende de implementaciones concretas.
 * </p>
 *
 * <strong>Nota</strong> La definicion de "pendiente" depende del esquema de outbox.
 * Tipicamente se usa un status o processedAt. Para evitar reprocesos y soportar reintentos,
 * se recomienda incluir mecanismo de locking y orden por createdAt.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public interface OutboxRepository {
    List<OutboxEvent> buscarPendientes();
    void marcarComoProcesado(OutboxEvent evento);
}