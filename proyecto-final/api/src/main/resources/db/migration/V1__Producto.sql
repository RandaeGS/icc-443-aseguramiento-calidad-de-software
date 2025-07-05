CREATE SEQUENCE IF NOT EXISTS producto_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE producto
(
    id              BIGINT           NOT NULL,
    nombre          VARCHAR(100)     NOT NULL,
    descripcion     VARCHAR(100)     NOT NULL,
    precio          DOUBLE PRECISION NOT NULL,
    costo           DOUBLE PRECISION NOT NULL,
    beneficio       DOUBLE PRECISION NOT NULL,
    cantidadInicial BIGINT           NOT NULL,
    CONSTRAINT pk_producto PRIMARY KEY (id)
);

ALTER TABLE producto
    ADD CONSTRAINT uc_producto_nombre UNIQUE (nombre);