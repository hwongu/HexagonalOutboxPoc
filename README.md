# 🚀 HexagonalOutboxPoc – Patrón Transactional Outbox

Este repositorio contiene la implementación de referencia del patrón **Transactional Outbox** utilizando **Java 21** y **Arquitectura Hexagonal**.

El proyecto demuestra cómo resolver el problema de la **"Doble Escritura" (Dual Write)** en sistemas distribuidos, garantizando consistencia eventual entre una base de datos moderna (Cloud) y un sistema heredado (Legacy) sin acoplar el dominio ni perder datos ante fallos de red.

---

## 🎯 El Problema: "Dual Write"
Cuando intentamos guardar en una base de datos local y notificar a otro sistema (o enviar a Kafka) en el mismo proceso, corremos un riesgo fatal:
1.  Se guarda el cambio en la BD Local. ✅
2.  Falla la red antes de notificar al sistema externo. ❌
3.  **Resultado:** Inconsistencia de datos. El sistema externo nunca se entera del cambio.

## ✅ La Solución: Transactional Outbox
En lugar de enviar el mensaje directamente, lo guardamos en una tabla auxiliar (`outbox`) dentro de la **misma transacción local** de la base de datos.
> *"O se guardan los dos (Cliente + Evento), o no se guarda ninguno."*

Luego, un proceso asíncrono (**Worker**) lee los eventos y los procesa con seguridad.

---

## 📁 Estructura del Proyecto

Este repositorio simula un entorno de microservicios dividido en dos componentes dentro de la carpeta `1_BackEnd`:

### 1️⃣ Componente A: El Productor (`PocOutbox`)
Es la aplicación principal (Core Aplicativo).
* **Responsabilidad:** Recibe la petición del usuario y guarda atómicamente al `Cliente` y al `Evento` en PostgreSQL.
* **Tecnología:** Java 21, JDBC Puro (sin frameworks pesados), Hexagonal Architecture.
* **Ubicación Clave:** `infrastructure/adapter/database/ClienteRepositoryDbCloud.java` (Aquí esta implementado el patron`).

### 2️⃣ Componente B: El Consumidor (`PocOutboxWorker`)
Es el proceso en segundo plano (Worker/Cron).
* **Responsabilidad:** Despierta cada X segundos, lee la tabla `outbox` buscando eventos `processed = false`, los envía al sistema Legacy y marca el evento como procesado.
* **Tecnología:** Java 21, ScheduledExecutorService.
* **Resiliencia:** Si el Legacy cae, el worker reintenta en el siguiente ciclo sin perder datos.

---

## 🛠 Instalación y Base de Datos

El proyecto requiere **PostgreSQL**. Ejecuta los scripts ubicados en la carpeta `2_DataBase`:

1.  **`Db_OnPremise.sql`**: Crea la base de datos simulada del sistema antiguo.
2.  **`Db_Cloud.sql`**: Crea la base de datos nueva e incluye la tabla crítica:

```sql
CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL, -- Ej: 'CLIENTE'
    aggregate_id VARCHAR(50) NOT NULL,   -- ID del Cliente
    type VARCHAR(50) NOT NULL,           -- Ej: 'CLIENTE_CREADO'
    payload TEXT NOT NULL,               -- JSON con los datos
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN DEFAULT FALSE      -- Estado del mensaje
);