package net.hwongu.poc.worker.domain.port;

/**
 * <strong>Rol Puerto de salida para escritura en sistema Legacy</strong>
 *
 * <p>
 * Actuo como contrato del dominio del worker hacia el repositorio Legacy.
 * Defino la operacion minima necesaria para replicar los datos de negocio en la base On Premise.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Desacoplar la logica de sincronizacion del detalle de acceso a datos.
 * La implementacion concreta (por ejemplo JDBC) vive en Infrastructure Adapter.
 * </p>
 *
 * <strong>Nota</strong> Para simplificar la POC se reciben datos sueltos (nombre y saldo).
 * En un escenario mas robusto se recomienda recibir un objeto de dominio o un DTO de integracion
 * y manejar idempotencia, validaciones y manejo de errores.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public interface LegacyRepository {
    // Recibe el JSON crudo o un objeto mapeado, para simplificar usaremos datos sueltos
    void guardarEnLegacy(String nombre, Double saldo);
}
