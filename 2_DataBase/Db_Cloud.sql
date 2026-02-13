/*
Autor Henry Wong
GitHub hwongu

Descripción
Script de ejemplo para crear la base de datos Db_Cloud, crear la tabla cliente, cargar un registro inicial y crear una tabla outbox para un patrón Outbox de mensajería
*/

-- Paso previo
-- Crea la base de datos si aún no existe
CREATE DATABASE "Db_Cloud";

-- Nota
-- A partir de aquí ejecuta el resto del script conectado a la base de datos Db_Cloud

-- 1 Limpieza
-- Elimina la tabla cliente si existe para que el script sea idempotente en entornos de prueba
DROP TABLE IF EXISTS cliente;

-- 2 Creación de la tabla cliente
-- Tabla principal de clientes con identificador autoincremental
CREATE TABLE cliente (
    id_cliente  SERIAL PRIMARY KEY,     -- Identificador único del cliente
    razon_social VARCHAR(150),           -- Nombre o razón social del cliente
    saldo_actual DECIMAL(15, 2)          -- Saldo actual con 2 decimales
);

-- 3 Inserción de datos
-- Inserta un cliente de ejemplo con saldo inicial
INSERT INTO cliente (razon_social, saldo_actual)
VALUES ('Bodega Don Pepe S.A.', 500.00);

-- 4 Verificación
-- Consulta para validar que el registro fue insertado correctamente
SELECT * FROM cliente;

-- 5 Tabla outbox
-- Tabla para registrar eventos de dominio y permitir su publicación asíncrona por un worker
CREATE TABLE outbox (
    id UUID PRIMARY KEY,                -- Identificador único del evento
    aggregate_type VARCHAR(50) NOT NULL, -- Tipo de agregado por ejemplo CLIENTE
    aggregate_id   VARCHAR(50) NOT NULL, -- Identificador del agregado afectado por ejemplo el id del cliente
    type           VARCHAR(50) NOT NULL, -- Tipo de evento por ejemplo CLIENTE_CREADO
    payload        TEXT NOT NULL,        -- Contenido del evento normalmente un JSON serializado
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha de creación del registro
    processed      BOOLEAN DEFAULT FALSE -- Marca para indicar si el evento ya fue procesado por el worker
);
