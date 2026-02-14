package net.hwongu.poc.worker.infrastructure.adapter;

import net.hwongu.poc.worker.domain.model.OutboxEvent;
import net.hwongu.poc.worker.domain.port.OutboxRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <strong>Rol Adaptador de infraestructura para lectura y marcado de Outbox via JDBC</strong>
 *
 * <p>
 * Actuo como implementacion concreta del puerto {@link OutboxRepository}.
 * Me conecto a la base Cloud donde vive la tabla outbox, consulto eventos pendientes
 * y marco como procesados los eventos ya sincronizados hacia Legacy.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Encapsular el acceso SQL y JDBC dentro de Infrastructure, manteniendo el servicio del worker
 * libre de detalles de persistencia. Esto facilita cambiar la implementacion a otro mecanismo
 * como ORM, API, o mensajeria, sin afectar la logica de negocio.
 * </p>
 *
 * <strong>Nota</strong> En esta POC se usa un flag boolean processed y un LIMIT 10.
 * En escenarios reales se recomienda ordenar por createdAt, aplicar locking para evitar
 * multiples workers procesando el mismo evento, y registrar errores o reintentos.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public class JdbcOutboxRepository implements OutboxRepository {

    // Conecta a la BD CLOUD (donde está la tabla outbox)
    private final String URL = "jdbc:postgresql://localhost:5440/Db_Cloud";
    private final String USER = "postgres";
    private final String PASS = "clave";

    @Override
    public List<OutboxEvent> buscarPendientes() {
        List<OutboxEvent> eventos = new ArrayList<>();
        String sql = "SELECT id, type, payload FROM outbox WHERE processed = false LIMIT 10";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                eventos.add(new OutboxEvent(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("type"),
                        rs.getString("payload")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eventos;
    }

    @Override
    public void marcarComoProcesado(OutboxEvent evento) {
        String sql = "UPDATE outbox SET processed = true WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, evento.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
