package net.hwongu.poc;

import net.hwongu.poc.worker.application.service.SincronizadorService;
import net.hwongu.poc.worker.infrastructure.adapter.JdbcLegacyRepository;
import net.hwongu.poc.worker.infrastructure.adapter.JdbcOutboxRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <strong>Rol Punto de arranque del worker y scheduler de sincronizacion</strong>
 *
 * <p>
 * Actuo como entrypoint del proceso worker. Cableo dependencias de forma manual,
 * construyo el {@link SincronizadorService} y programo su ejecucion periodica cada 15 segundos
 * usando {@link ScheduledExecutorService}.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Separar el loop de ejecucion y la infraestructura de scheduling de la logica de negocio.
 * El servicio realiza el flujo Outbox hacia Legacy, mientras este main solo gestiona arranque
 * y periodicidad.
 * </p>
 *
 * <strong>Nota</strong> Se usa un solo hilo para preservar orden y evitar paralelismo no controlado.
 * En escenarios reales se recomienda shutdown controlado, metricas, healthchecks y mecanismos de lock
 * para ejecucion distribuida si existen multiples instancias del worker.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("INICIANDO WORKER DE SINCRONIZACIÓN");
        System.out.println("=========================================");

        // 1. Inyección de Dependencias (Manual)
        var outboxRepo = new JdbcOutboxRepository();
        var legacyRepo = new JdbcLegacyRepository();
        var servicio = new SincronizadorService(outboxRepo, legacyRepo);

        // 2. Configuración del Scheduler (Cron)
        // Usamos un solo hilo porque el orden importa en sincronización
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // 3. Programación: Ejecutar cada 15 segundos
        // initialDelay: 0, period: 15, unit: SECONDS
        scheduler.scheduleAtFixedRate(() -> {
            try {
                servicio.ejecutarSincronizacion();
            } catch (Exception e) {
                System.err.println("Error fatal en el ciclo del worker: " + e.getMessage());
            }
        }, 0, 15, TimeUnit.SECONDS);
    }
}