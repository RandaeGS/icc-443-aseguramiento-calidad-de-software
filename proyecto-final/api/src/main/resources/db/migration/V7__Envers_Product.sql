CREATE SEQUENCE IF NOT EXISTS revinfo_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE producto_aud
(
    rev         INTEGER NOT NULL,
    revtype     SMALLINT,
    id          BIGINT  NOT NULL,
    name        VARCHAR(255),
    description VARCHAR(255),
    category    VARCHAR(255),
    price       DOUBLE PRECISION,
    cost        DOUBLE PRECISION,
    profit      DOUBLE PRECISION,
    quantity    BIGINT,
    is_active   BOOLEAN,
    CONSTRAINT pk_producto_aud PRIMARY KEY (rev, id)
);

CREATE TABLE revinfo
(
    rev      INTEGER NOT NULL,
    revtstmp BIGINT,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

ALTER TABLE producto_aud
    ADD CONSTRAINT FK_PRODUCTO_AUD_ON_REV FOREIGN KEY (rev) REFERENCES revinfo (rev);