package net.hwongu.poc;

import net.hwongu.poc.application.service.ConsultaSaldoService;
import net.hwongu.poc.application.service.CrearClienteService;
import net.hwongu.poc.domain.port.out.ClienteRepository;
import net.hwongu.poc.infrastructure.adapter.database.ClienteRepositoryDbCloud;
import net.hwongu.poc.infrastructure.adapter.database.ClienteRepositoryDbOnPremise;
import net.hwongu.poc.infrastructure.adapter.mock.ClienteRepositoryMock;

import java.util.Scanner;

/**
 * <strong>Rol Adaptador de entrada por consola para ejecutar casos de uso</strong>
 *
 * <p>
 * Actuo como punto de arranque de la aplicacion y como interfaz por consola.
 * Presento un menu, recojo datos del usuario y ejecuto servicios de aplicacion
 * {@link CrearClienteService} y {@link ConsultaSaldoService}.
 * </p>
 *
 * <p>
 * <strong>Proposito arquitectonico</strong><br>
 * Mantener la interaccion con el usuario y la seleccion de dependencias fuera del dominio.
 * Desde aqui se decide la implementacion del puerto {@link ClienteRepository}
 * para trabajar con Legacy, Cloud con Outbox, o un Mock en memoria.
 * </p>
 *
 * <strong>Nota</strong> La seleccion de repositorio se realiza en tiempo de ejecucion segun la opcion del menu.
 * En un escenario real esto suele resolverse por configuracion e inyeccion de dependencias.
 *
 * <p>
 * <strong>GitHub</strong> hwongu
 * </p>
 *
 * @author Henry Wong
 */
public class App {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        int opcion;

        System.out.println("=========================================");
        System.out.println("POC OUTBOX PATTERN - INICIADA");
        System.out.println("=========================================");

        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opción: ");
            ClienteRepository repositorio = seleccionarRepositorio(opcion);
            if (repositorio != null) {
                if (opcion >= 1 && opcion <= 3) {
                    procesarInsercion(repositorio, opcion == 2);
                }
                else if (opcion >= 4 && opcion <= 6) {
                    procesarConsulta(repositorio);
                }
            } else if (opcion != 7) {
                System.out.println("Opción no válida.");
            }

        } while (opcion != 7);
        System.out.println("Aplicación finalizada.");
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENU DE OPCIONES ---");
        System.out.println("1. Insertar Cliente (Legacy/OnPremise)");
        System.out.println("2. Insertar Cliente (Cloud + OUTBOX)");
        System.out.println("3. Insertar Cliente (Mock/Memoria)");
        System.out.println("--------------------------------");
        System.out.println("4. Consultar Saldo (Legacy/OnPremise)");
        System.out.println("5. Consultar Saldo (Cloud)");
        System.out.println("6. Consultar Saldo (Mock/Memoria)");
        System.out.println("7. Salir");
    }

    private static ClienteRepository seleccionarRepositorio(int opcion) {
        switch (opcion) {
            case 1:
            case 4:
                return new ClienteRepositoryDbOnPremise();
            case 2:
            case 5:
                // Esta instancia contiene la lógica de Transactional Outbox
                return new ClienteRepositoryDbCloud();
            case 3:
            case 6:
                return new ClienteRepositoryMock();
            default:
                return null;
        }
    }

    private static void procesarInsercion(ClienteRepository repositorio, boolean esOutbox) {
        System.out.println("\n--- NUEVO CLIENTE ---");
        if (esOutbox) {
            System.out.println("Nota: Al guardar aquí, se generará también un evento en la tabla 'outbox'.");
        }
        String nombre = leerTexto("Ingrese nombre (Razón Social): ");
        Double saldo = leerDouble("Ingrese saldo inicial: ");
        CrearClienteService servicio = new CrearClienteService(repositorio);
        try {
            servicio.ejecutar(nombre, saldo);
            if (esOutbox) {
                System.out.println("Cliente y Evento Outbox guardados atómicamente.");
            }
        } catch (Exception e) {
            System.err.println("Error al procesar la transacción: " + e.getMessage());
        }
    }

    private static void procesarConsulta(ClienteRepository repositorio) {
        System.out.println("\n--- CONSULTA DE SALDO ---");
        int id = leerEntero("Ingrese ID de Cliente: ");

        ConsultaSaldoService servicio = new ConsultaSaldoService(repositorio);
        servicio.ejecutarConsulta(id);
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine();
    }

    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);
        while (!sc.hasNextInt()) {
            System.out.println("Por favor, ingrese un número válido.");
            sc.next();
            System.out.print(mensaje);
        }
        int valor = sc.nextInt();
        sc.nextLine();
        return valor;
    }

    private static double leerDouble(String mensaje) {
        System.out.print(mensaje);
        while (!sc.hasNextDouble()) {
            System.out.println("Por favor, ingrese un monto válido (ej. 1500.50).");
            sc.next();
            System.out.print(mensaje);
        }
        double valor = sc.nextDouble();
        sc.nextLine();
        return valor;
    }
}