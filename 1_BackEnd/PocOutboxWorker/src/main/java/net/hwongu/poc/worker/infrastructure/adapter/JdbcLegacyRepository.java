package net.hwongu.poc.worker.infrastructure.adapter;

import net.hwongu.poc.worker.domain.port.LegacyRepository;
import java.sql.*;

/**
 * <strong>Rol Adaptador de infraestructura para escritura en Legacy via JDBC</strong>
 *
 * <p>
 * Actuo como implementacion concreta del puerto {@link LegacyRepository}.
 * Escribo en la base de datos Legacy (On Premise) usando JDBC, traduciendo los datos del evento
 * a la estructura antigua en ingles.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Encapsular el detalle de integracion con el sistema Legacy dentro de Infrastructure.
 * Esta clase puede ser reemplazada por otro mecanismo (API REST, SOAP, mensajeria, etc)
 * sin cambiar la logica del worker.
 * </p>
 *
 * <strong>Nota</strong> En esta POC la conexion se crea por llamada y las credenciales estan embebidas.
 * En un escenario real se recomienda externalizar configuracion, usar pool de conexiones
 * y aplicar politicas de reintento e idempotencia para evitar duplicados.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public class JdbcLegacyRepository implements LegacyRepository {

    // Conecta a la BD LEGACY (On-Premise)
    private final String URL = "jdbc:postgresql://localhost:5441/Db_OnPremise";
    private final String USER = "postgres";
    private final String PASS = "clave";

    @Override
    public void guardarEnLegacy(String nombre, Double saldo) {
        // Usamos la estructura antigua en ingles: "customers" con campos "business_name" y "total_balance"
        String sql = "INSERT INTO customers (business_name, total_balance) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setDouble(2, saldo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error escribiendo en Legacy: " + e.getMessage(), e);
        }
    }
}
