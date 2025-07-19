CREATE TABLE stock_movements
(
    id             BIGINT NOT NULL,
    producto_id    BIGINT,
    username       VARCHAR(255),
    date           TIMESTAMP WITHOUT TIME ZONE,
    actualQuantity BIGINT,
    quantityChange BIGINT,
    CONSTRAINT pk_stockmovement PRIMARY KEY (id)
);

ALTER TABLE stock_movements
    ADD CONSTRAINT FK_STOCKMOVEMENT_ON_PRODUCTO FOREIGN KEY (producto_id) REFERENCES Producto (id);
