package net.hwongu.poc.infrastructure.adapter.database;

import net.hwongu.poc.domain.model.Cliente;
import net.hwongu.poc.domain.port.out.ClienteRepository;

import java.sql.*;
import java.util.UUID;

/**
 * <strong>Rol Adaptador de infraestructura Cloud con soporte de Outbox</strong>
 *
 * <p>
 * Actuo como implementacion concreta del puerto de salida {@link ClienteRepository} para PostgreSQL en Cloud.
 * Persisto la entidad {@link Cliente} y registro un evento en la tabla outbox dentro de la misma transaccion.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Encapsular JDBC y el esquema Cloud, y habilitar integracion asincrona mediante el patron Outbox.
 * Con esto se soporta consistencia eventual hacia otros sistemas sin acoplar el dominio a mensajeria.
 * </p>
 *
 * <strong>Nota</strong> Esta implementacion garantiza atomicidad local entre cliente y outbox en la misma base de datos.
 * No garantiza atomicidad distribuida entre multiples bases o servicios externos. Adicionalmente, esta POC usa
 * credenciales embebidas y salida por consola. En un escenario real se recomienda
 * externalizar configuracion, usar pool de conexiones, logging y manejo de errores consistente.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public class ClienteRepositoryDbCloud implements ClienteRepository {

    private final String URL = "jdbc:postgresql://localhost:5432/Db_Cloud";
    private final String USER = "postgres";
    private final String PASS = "clave";

    @Override
    public void guardar(Cliente cliente) {
        // SQL 1: Guardar la entidad de negocio
        String sqlCliente = "INSERT INTO cliente (razon_social, saldo_actual) VALUES (?, ?)";

        // SQL 2: Guardar el evento de dominio (Outbox)
        String sqlOutbox = "INSERT INTO outbox (id, aggregate_type, aggregate_id, type, payload) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            //1. Registramos el cliente y el evento en la misma transacción
            PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS);
            stmtCliente.setString(1, cliente.getNombre());
            stmtCliente.setDouble(2, cliente.getSaldo());
            stmtCliente.executeUpdate();
            int idGenerado = 0;
            try (ResultSet rs = stmtCliente.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1); // PostgreSQL devuelve el ID insertado
                }
            }

            // Construimos el payload del evento. En un caso real, podríamos usar una librería JSON.
            String jsonPayload = String.format(
                    "{\"id\": %d, \"nombre\": \"%s\", \"saldo\": %.2f, \"origen\": \"CLOUD\"}",
                    idGenerado, cliente.getNombre(), cliente.getSaldo()
            );
            // Insertamos el evento en la tabla outbox
            PreparedStatement stmtOutbox = conn.prepareStatement(sqlOutbox);
            stmtOutbox.setObject(1, UUID.randomUUID()); // ID único del mensaje
            stmtOutbox.setString(2, "CLIENTE");         // Aggregate Type
            stmtOutbox.setString(3, String.valueOf(idGenerado)); // Aggregate ID
            stmtOutbox.setString(4, "CLIENTE_CREADO");  // Event Type
            stmtOutbox.setString(5, jsonPayload);       // Payload
            stmtOutbox.executeUpdate();

            // 2. COMMIT: Aquí se guardan AMBOS o NINGUNO. Esto garantiza atomicidad.
            conn.commit();
            System.out.println("[CLOUD + OUTBOX] Transacción Exitosa. Cliente ID: " + idGenerado);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.err.println("!! Error detectado. Ejecutando ROLLBACK...");
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @Override
    public Cliente buscarPorId(Integer id) {
        System.out.println("--- [CLOUD] Conectando a Base de Datos Cloud ---");
        String sql = "SELECT id_cliente, razon_social, saldo_actual FROM cliente WHERE id_cliente = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idEncontrado = rs.getInt("id_cliente");
                    String nombre = rs.getString("razon_social");
                    Double saldo = rs.getDouble("saldo_actual");
                    return new Cliente(idEncontrado, nombre, saldo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en Cloud: " + e.getMessage());
        }
        return null;
    }

}
