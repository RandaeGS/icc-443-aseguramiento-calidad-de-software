CREATE SEQUENCE IF NOT EXISTS Producto_seq START WITH 1 INCREMENT BY 50;

DROP TABLE producto;

CREATE TABLE producto
(
    id          BIGINT           NOT NULL,
    name        VARCHAR(100)     NOT NULL,
    description VARCHAR(100)     NOT NULL,
    category    VARCHAR(255)     NOT NULL,
    price       DOUBLE PRECISION NOT NULL,
    cost        DOUBLE PRECISION NOT NULL,
    profit      DOUBLE PRECISION NOT NULL,
    quantity    BIGINT           NOT NULL,
    is_active   BOOLEAN          NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_producto PRIMARY KEY (id)
);

ALTER TABLE producto
    ADD CONSTRAINT uc_producto_name UNIQUE (name);