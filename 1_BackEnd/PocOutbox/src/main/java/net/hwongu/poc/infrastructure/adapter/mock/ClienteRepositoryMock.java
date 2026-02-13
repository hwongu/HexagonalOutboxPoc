package net.hwongu.poc.infrastructure.adapter.mock;

import net.hwongu.poc.domain.model.Cliente;
import net.hwongu.poc.domain.port.out.ClienteRepository;

public class ClienteRepositoryMock implements ClienteRepository {

    @Override
    public void guardar(Cliente cliente) {
        System.out.println("[MOCK] Guardando en memoria simulada: " + cliente.getNombre());
        System.out.println("[MOCK] ... ¡Guardado!");
    }

    @Override
    public Cliente buscarPorId(Integer id) {
        System.out.println("--- [MOCK] Simulando busqueda ---");
        if (id == 1) return new Cliente(1, "Empresa Demo S.A.", 50000.00);
        return null;
    }

}
