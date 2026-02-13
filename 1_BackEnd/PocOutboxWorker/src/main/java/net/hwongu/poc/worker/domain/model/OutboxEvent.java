package net.hwongu.poc.worker.domain.model;

import java.util.UUID;

/**
 * <strong>Rol Modelo de dominio para evento Outbox</strong>
 *
 * <p>
 * Represento un evento persistido en la tabla outbox dentro de la base Cloud.
 * Contengo el identificador unico del mensaje, el tipo de evento y el payload en formato texto
 * normalmente JSON.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Transportar datos de integracion de forma simple entre el puerto {@code OutboxRepository}
 * y el servicio de aplicacion del worker, manteniendo el dominio libre de JDBC y SQL.
 * </p>
 *
 * <strong>Nota</strong> Este record modela solo los campos minimos necesarios para el worker.
 * Si el esquema real incluye campos como aggregateId, aggregateType, status, createdAt,
 * se pueden agregar al record para soportar filtros, orden, reintentos e idempotencia.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public record OutboxEvent(UUID id, String type, String payload) {}