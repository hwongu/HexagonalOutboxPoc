package net.hwongu.poc.worker.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwongu.poc.worker.domain.model.OutboxEvent;
import net.hwongu.poc.worker.domain.port.LegacyRepository;
import net.hwongu.poc.worker.domain.port.OutboxRepository;

import java.util.List;

/**
 * <strong>Rol Orquestador de sincronizacion Outbox hacia Legacy</strong>
 *
 * <p>
 * Actuo como servicio de aplicacion del worker. Leo eventos pendientes desde el puerto
 * {@link OutboxRepository}, transformo el payload JSON a datos de negocio y ejecuto la escritura
 * en la base legacy mediante {@link LegacyRepository}. Luego marco el evento como procesado
 * para evitar reprocesos.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Implementar el patron Transactional Outbox en modo asincrono.
 * La aplicacion Cloud escribe entidad y evento en una misma transaccion, y este worker
 * realiza la entrega eventual hacia Legacy, desacoplando el dominio de la estrategia de migracion.
 * </p>
 *
 * <strong>Flujo</strong>
 * <p>
 * 1 Obtener eventos pendientes desde Outbox<br>
 * 2 Parsear el payload JSON (nombre y saldo)<br>
 * 3 Guardar en Legacy<br>
 * 4 Marcar como procesado en Outbox
 * </p>
 *
 * <strong>Nota</strong> La operacion debe ser idempotente o tolerante a reintentos.
 * Si falla la escritura en Legacy o el marcado de procesado, el evento podria reprocesarse.
 * Una mejora tipica es agregar reintentos, backoff y dead letter queue.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public class SincronizadorService {

    private final OutboxRepository outboxRepo;
    private final LegacyRepository legacyRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public SincronizadorService(OutboxRepository outboxRepo, LegacyRepository legacyRepo) {
        this.outboxRepo = outboxRepo;
        this.legacyRepo = legacyRepo;
    }

    public void ejecutarSincronizacion() {
        System.out.println("[WORKER] Buscando eventos pendientes...");
        List<OutboxEvent> eventos = outboxRepo.buscarPendientes();
        if (eventos.isEmpty()) {
            System.out.println("[WORKER] Nada por hacer.");
            return;
        }
        for (OutboxEvent evento : eventos) {
            try {
                System.out.println("[WORKER] Procesando evento ID: " + evento.id());
                // 1. Parsear el Payload (JSON) que viene de la Cloud
                JsonNode nodo = mapper.readTree(evento.payload());
                String nombre = nodo.get("nombre").asText();
                Double saldo = nodo.get("saldo").asDouble();
                // 2. Escribir en Legacy (On-Premise)
                legacyRepo.guardarEnLegacy(nombre, saldo);
                // 3. Marcar evento como procesado en Cloud
                outboxRepo.marcarComoProcesado(evento);
                System.out.println("[WORKER] Sincronización completa para: " + nombre);
            } catch (Exception e) {
                System.err.println("[WORKER] Error procesando evento " + evento.id() + ": " + e.getMessage());
                // Aquí podrías implementar una lógica de "Dead Letter Queue" o reintentos
            }
        }
    }
}