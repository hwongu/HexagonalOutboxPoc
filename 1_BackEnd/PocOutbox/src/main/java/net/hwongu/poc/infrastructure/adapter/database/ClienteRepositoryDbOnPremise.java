package net.hwongu.poc.infrastructure.adapter.database;

import net.hwongu.poc.domain.model.Cliente;
import net.hwongu.poc.domain.port.out.ClienteRepository;

import java.sql.*;

/**
 * <strong>Rol Adaptador de infraestructura para repositorio On Premise usando JDBC</strong>
 *
 * <p>
 * Actuo como implementacion concreta del puerto de salida {@link ClienteRepository} para una base de datos On Premise
 * basada en PostgreSQL. Traduzco las operaciones del dominio a SQL orientado al esquema legacy en ingles
 * y mapeo los resultados a {@link Cliente}.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Aislar el detalle de persistencia legacy en la capa Infrastructure, permitiendo que el dominio trabaje con un modelo
 * unificado mientras la migracion convive con dos esquemas distintos.
 * </p>
 *
 * <strong>Nota</strong> Esta POC usa credenciales embebidas y salida por consola. En un escenario real se recomienda
 * externalizar configuracion, usar pool de conexiones, logging y manejo de errores consistente.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public class ClienteRepositoryDbOnPremise implements ClienteRepository {

    private final String URL = "jdbc:postgresql://localhost:5441/Db_OnPremise";
    private final String USER = "postgres";
    private final String PASS = "clave";

    @Override
    public void guardar(Cliente cliente) {
        String sql = "INSERT INTO customers (business_name, total_balance) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNombre());
            stmt.setDouble(2, cliente.getSaldo());
            stmt.executeUpdate();
            System.out.println("[ONPREMISE] INSERT exitoso en tabla 'customers'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Cliente buscarPorId(Integer id) {
        System.out.println("--- [ONPREMISE] Conectando a Base de Datos On Premise ---");
        String sql = "SELECT id, business_name, total_balance FROM customers WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Cliente(
                        rs.getInt("id"),
                        rs.getString("business_name"),
                        rs.getDouble("total_balance")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
