CREATE SEQUENCE IF NOT EXISTS Producto_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE Producto
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

ALTER TABLE Producto
    ADD CONSTRAINT uc_producto_nombre UNIQUE (nombre);