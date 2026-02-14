# 🚀 HexagonalOutboxPoc – Patrón Transactional Outbox

Este repositorio contiene la implementación de referencia del patrón **Transactional Outbox** utilizando **Java 21** y **Arquitectura Hexagonal**.

El proyecto demuestra cómo resolver el problema de la **"Doble Escritura" (Dual Write)** en sistemas distribuidos, garantizando consistencia eventual entre una base de datos moderna (Cloud) y un sistema heredado (Legacy) sin acoplar el dominio ni perder datos ante fallos de red.

---

## 🎯 Contexto: El Problema del "Dual Write"

Cuando intentamos guardar en una base de datos local y notificar a otro sistema en el mismo proceso, corremos un riesgo fatal:
1.  Se guarda el cambio en la BD Local. ✅
2.  Falla la red antes de notificar al sistema externo. ❌
3.  **Resultado:** Inconsistencia de datos. El sistema externo nunca se entera del cambio.

### ✅ La Solución: Transactional Outbox
En lugar de enviar el mensaje directamente, lo guardamos en una tabla auxiliar (`outbox`) dentro de la **misma transacción local** de la base de datos.
> *"O se guardan los dos (Cliente + Evento), o no se guarda ninguno."*

Luego, un proceso asíncrono (**Worker**) lee los eventos y los procesa con seguridad.

---

## 📁 Estructura del Repositorio

El proyecto se divide en tres componentes físicos principales:

### 1️⃣ 1_BackEnd (El Código Java)
Ubicación: `/1_BackEnd`
Contiene la lógica de negocio dividida en dos roles clave:

* **Componente A: El Productor (`PocOutbox`)**
    * Es el Core Aplicativo. Recibe la petición y guarda atómicamente al `Cliente` y al `Evento` en PostgreSQL.
    * **Ubicación Clave:** `infrastructure/adapter/database/ClienteRepositoryDbCloud.java` (Aquí se implementa la transacción atómica).
* **Componente B: El Consumidor (`PocOutboxWorker`)**
    * Es el proceso en segundo plano (Worker/Cron).
    * Despierta periódicamente, lee la tabla `outbox` buscando eventos `processed = false`, los replica al sistema Legacy y marca el evento como procesado.

### 2️⃣ 2_DataBase (Scripts de Referencia)
Ubicación: `/2_DataBase`
Contiene los scripts SQL crudos (`.sql`) para referencia manual:
* `Db_OnPremise.sql`: Crea el entorno Legacy.
* `Db_Cloud.sql`: Crea el entorno Cloud e incluye la tabla crítica:
    ```sql
    CREATE TABLE outbox (
        id UUID PRIMARY KEY,
        aggregate_type VARCHAR(50), -- Ej: 'CLIENTE'
        payload TEXT NOT NULL,      -- JSON del evento
        processed BOOLEAN DEFAULT FALSE
    );
    ```

### 3️⃣ 3_Infrastructure (Automatización Docker)
Ubicación: `/3_Infrastructure/PocAutomated`
Contiene la **Infraestructura como Código (IaC)** para levantar los entornos simulados.
* `docker-compose.yml`: Orquesta las dos bases de datos (Cloud con Outbox y Legacy).
* `sql/`: Scripts de inicialización automática para los contenedores.

---

## 🚀 Guía de Ejecución (Entorno Dockerizado)

Para validar esta PoC, la infraestructura está automatizada mediante contenedores.

1.  **Infraestructura:** Navega a la carpeta `3_Infrastructure/PocAutomated` y levanta los servicios utilizando tu orquestador de contenedores.
    * **Cloud DB:** Quedará expuesta en el puerto `5440`.
    * **Legacy DB:** Quedará expuesta en el puerto `5441`.

2.  **Aplicación:** Una vez que las bases de datos estén activas, abre el proyecto ubicado en `1_BackEnd` con tu IDE y ejecuta la aplicación. Primero iniciando el PocOutboxWorker para que este en modo de escucha y luego prueba con el App.java del proyecto PocOutbox

---

## 🐳 Comandos Docker

Para levantar toda la infraestructura de las bases de datos: 

docker-compose up -d

Para detener los servicios y eliminar los volúmenes de datos (reset completo):

docker-compose down -v

---

## ⚠️ Nota Técnica (Trade-offs)

Esta implementación utiliza el patrón **Polling Publisher** (El worker consulta la BD).
1.  **Latencia:** No es tiempo real. Hay un retraso (`pollingInterval`) entre que se guarda el dato y se procesa.
2.  **Carga en BD:** Consultar constantemente la tabla `outbox` genera tráfico en la base de datos, incluso si está vacía.
3.  **Escalabilidad:** Si hay múltiples workers, se requiere un mecanismo de bloqueo (optimistic locking o `SKIP LOCKED`) para que no procesen el mismo evento dos veces.

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 21 (Records, Text Blocks).
* **Arquitectura:** Hexagonal (Ports & Adapters).
* **Patrones Clave:**
    * **Transactional Outbox** (Persistencia atómica de eventos).
    * **Polling Publisher** (Estrategia del Worker).
* **Base de Datos:** PostgreSQL 15.
* **Infraestructura:** Docker & Docker Compose.

---

**Author:** [Henry Wong](https://github.com/hwongu)

---

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=for-the-badge&logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql)
