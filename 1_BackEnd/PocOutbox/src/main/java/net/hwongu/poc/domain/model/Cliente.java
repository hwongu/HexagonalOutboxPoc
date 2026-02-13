package net.hwongu.poc.domain.model;

/**
 * <strong>Rol Entidad de dominio Cliente</strong>
 *
 * <p>
 * Represento al cliente dentro del core de negocio. Contengo los atributos minimos
 * que los casos de uso requieren para operar, como identificador, nombre y saldo.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Mantener el modelo de negocio libre de dependencias de frameworks o infraestructura.
 * Este objeto puede viajar entre casos de uso, puertos y adaptadores sin acoplamiento tecnologico.
 * </p>
 *
 * <strong>Nota</strong> El metodo {@link #toString()} brinda una representacion simple para consola o logging
 * en contexto de POC.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public class Cliente {
    private Integer id;
    private String nombre;
    private Double saldo;

    public Cliente(Integer id, String nombre, Double saldo) {
        this.id = id;
        this.nombre = nombre;
        this.saldo = saldo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    @Override
    public String toString() {
        return "Cliente: " + nombre + " | Saldo: $" + saldo;
    }
}
