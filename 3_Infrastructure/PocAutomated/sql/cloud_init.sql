-- cloud_init.sql
-- Base de Datos: Db_Cloud (Moderno)
-- Patrón: Transactional Outbox

-- 1. Limpieza
DROP TABLE IF EXISTS outbox;
DROP TABLE IF EXISTS cliente;

-- 2. Tabla de Negocio
CREATE TABLE cliente (
    id_cliente SERIAL PRIMARY KEY,
    razon_social VARCHAR(150),
    saldo_actual DECIMAL(15, 2)
);

-- 3. Tabla OUTBOX 
-- Aqui se guardarán los eventos en la misma transaccion que el cliente
CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL, -- Ej: CLIENTE
    aggregate_id   VARCHAR(50) NOT NULL, -- Ej: ID del cliente creado
    type           VARCHAR(50) NOT NULL, -- Ej: CLIENTE_CREADO
    payload        TEXT NOT NULL,        -- JSON del evento
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed      BOOLEAN DEFAULT FALSE -- Para que el Worker sepa que procesar
);

-- 4. Datos Iniciales
ALTER SEQUENCE cliente_id_cliente_seq RESTART WITH 1001;

INSERT INTO cliente (razon_social, saldo_actual) 
VALUES ('Bodega Don Pepe S.A. (Cloud)', 500.00);