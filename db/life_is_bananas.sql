-- =====================================================================
-- Schema en startgegevens van Life is Bananas.
--
-- De toepassing staat op spring.jpa.hibernate.ddl-auto=none: dit script
-- is dus de enige bron van het schema. Pas je een entiteit aan, pas dan
-- ook dit script aan.
--
-- Inlezen:
--   mysql -u root -p < db/life_is_bananas.sql
-- =====================================================================

-- Database en gebruiker. Haal het commentaar weg als die nog niet bestaan.
-- CREATE DATABASE IF NOT EXISTS life_is_bananas
--   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- CREATE USER IF NOT EXISTS 'lifeisbananasuser'@'localhost'
--   IDENTIFIED BY 'lifeisbananasuserpw';
-- GRANT ALL PRIVILEGES ON life_is_bananas.* TO 'lifeisbananasuser'@'localhost';
-- FLUSH PRIVILEGES;

USE life_is_bananas;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS parameters;
DROP TABLE IF EXISTS freeze_dry_instructions;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS ingredients;
DROP TABLE IF EXISTS formulas;
DROP TABLE IF EXISTS raw_materials;
DROP TABLE IF EXISTS nutritional_values;
DROP TABLE IF EXISTS bio_engineers;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- bio_engineers
-- ---------------------------------------------------------------------
CREATE TABLE bio_engineers (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  name           VARCHAR(255) DEFAULT NULL,
  email          VARCHAR(255) DEFAULT NULL,
  phone_number   VARCHAR(255) DEFAULT NULL,
  specialization VARCHAR(255) DEFAULT NULL,
  password       VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_bio_engineer_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- nutritional_values (per 100 gram grondstof)
-- ---------------------------------------------------------------------
CREATE TABLE nutritional_values (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  calories      DOUBLE NOT NULL DEFAULT 0,
  proteins      DOUBLE NOT NULL DEFAULT 0,
  carbohydrates DOUBLE NOT NULL DEFAULT 0,
  fats          DOUBLE NOT NULL DEFAULT 0,
  fibers        DOUBLE NOT NULL DEFAULT 0,
  sugars        DOUBLE NOT NULL DEFAULT 0,
  salt          DOUBLE NOT NULL DEFAULT 0,
  vitamin_a     DOUBLE NOT NULL DEFAULT 0,
  vitamin_c     DOUBLE NOT NULL DEFAULT 0,
  calcium       DOUBLE NOT NULL DEFAULT 0,
  iron          DOUBLE NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- raw_materials
-- Fruit en groente staan samen in een tabel; material_type zegt welke
-- soort het is. De soortspecifieke kolommen blijven NULL voor de andere.
-- ---------------------------------------------------------------------
CREATE TABLE raw_materials (
  id                  BIGINT      NOT NULL AUTO_INCREMENT,
  material_type       VARCHAR(31) NOT NULL,
  name                VARCHAR(255)  DEFAULT NULL,
  organic_certified   BIT(1)      NOT NULL DEFAULT b'0',
  price               DECIMAL(10, 2) DEFAULT NULL,
  unit                VARCHAR(255)  DEFAULT NULL,
  nutritional_value_id BIGINT       DEFAULT NULL,
  -- enkel voor fruit
  ripeness_level      VARCHAR(255)  DEFAULT NULL,
  sugar_content       DOUBLE        DEFAULT NULL,
  acidity_level       DOUBLE        DEFAULT NULL,
  -- enkel voor groente
  firmness            VARCHAR(255)  DEFAULT NULL,
  moisture_content    DOUBLE        DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_raw_material_nutritional_value (nutritional_value_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- formulas (de recepturen)
-- ---------------------------------------------------------------------
CREATE TABLE formulas (
  id                 BIGINT       NOT NULL AUTO_INCREMENT,
  name               VARCHAR(255)  DEFAULT NULL,
  version            VARCHAR(255)  DEFAULT NULL,
  preparation_method VARCHAR(2000) DEFAULT NULL,
  creation_date      DATE          DEFAULT NULL,
  last_modified      DATE          DEFAULT NULL,
  status             VARCHAR(31)   DEFAULT NULL,
  bio_engineer_id    BIGINT        DEFAULT NULL,
  PRIMARY KEY (id),
  KEY i_formula_name (name),
  KEY fk_formula_bio_engineer (bio_engineer_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- ingredients (koppelt een grondstof aan een receptuur)
-- ---------------------------------------------------------------------
CREATE TABLE ingredients (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  quantity        DECIMAL(10, 2) DEFAULT NULL,
  unit            VARCHAR(31)    DEFAULT NULL,
  sequence        INT    NOT NULL DEFAULT 1,
  formula_id      BIGINT         DEFAULT NULL,
  raw_material_id BIGINT         DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_ingredient_formula (formula_id),
  KEY fk_ingredient_raw_material (raw_material_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- products
-- ---------------------------------------------------------------------
CREATE TABLE products (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  name            VARCHAR(255)  DEFAULT NULL,
  description     VARCHAR(2000) DEFAULT NULL,
  shelf_life      INT           DEFAULT NULL,
  weight          INT           DEFAULT NULL,
  batch_number    VARCHAR(255)  DEFAULT NULL,
  production_date DATE          DEFAULT NULL,
  status          VARCHAR(31)   DEFAULT NULL,
  formula_id      BIGINT        DEFAULT NULL,
  bio_engineer_id BIGINT        DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_product_formula (formula_id),
  KEY fk_product_bio_engineer (bio_engineer_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- freeze_dry_instructions
-- ---------------------------------------------------------------------
CREATE TABLE freeze_dry_instructions (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  formula_name    VARCHAR(255)  DEFAULT NULL,
  version         VARCHAR(255)  DEFAULT NULL,
  creation_date   DATE          DEFAULT NULL,
  steps           VARCHAR(4000) DEFAULT NULL,
  remarks         VARCHAR(2000) DEFAULT NULL,
  bio_engineer_id BIGINT        DEFAULT NULL,
  formula_id      BIGINT        DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_instruction_bio_engineer (bio_engineer_id),
  KEY fk_instruction_formula (formula_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- parameters
-- "value" is in sommige databanken een gereserveerd woord: vandaar
-- parameter_value.
-- ---------------------------------------------------------------------
CREATE TABLE parameters (
  id                         BIGINT NOT NULL AUTO_INCREMENT,
  name                       VARCHAR(255) DEFAULT NULL,
  parameter_value            VARCHAR(255) DEFAULT NULL,
  unit                       VARCHAR(255) DEFAULT NULL,
  minimum_limit              DOUBLE       DEFAULT NULL,
  maximum_limit              DOUBLE       DEFAULT NULL,
  critical                   BIT(1) NOT NULL DEFAULT b'0',
  freeze_dry_instruction_id  BIGINT       DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_parameter_instruction (freeze_dry_instruction_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Verwijssleutels
--
-- Compositie (de ene bestaat niet zonder de andere) krijgt CASCADE,
-- de overige verbanden RESTRICT.
-- ---------------------------------------------------------------------
ALTER TABLE raw_materials
  ADD CONSTRAINT fk_raw_material_nutritional_value
    FOREIGN KEY (nutritional_value_id) REFERENCES nutritional_values (id) ON DELETE CASCADE;

ALTER TABLE formulas
  ADD CONSTRAINT fk_formula_bio_engineer
    FOREIGN KEY (bio_engineer_id) REFERENCES bio_engineers (id) ON DELETE RESTRICT;

ALTER TABLE ingredients
  ADD CONSTRAINT fk_ingredient_formula
    FOREIGN KEY (formula_id) REFERENCES formulas (id) ON DELETE CASCADE,
  ADD CONSTRAINT fk_ingredient_raw_material
    FOREIGN KEY (raw_material_id) REFERENCES raw_materials (id) ON DELETE RESTRICT;

ALTER TABLE products
  ADD CONSTRAINT fk_product_formula
    FOREIGN KEY (formula_id) REFERENCES formulas (id) ON DELETE RESTRICT,
  ADD CONSTRAINT fk_product_bio_engineer
    FOREIGN KEY (bio_engineer_id) REFERENCES bio_engineers (id) ON DELETE RESTRICT;

ALTER TABLE freeze_dry_instructions
  ADD CONSTRAINT fk_instruction_bio_engineer
    FOREIGN KEY (bio_engineer_id) REFERENCES bio_engineers (id) ON DELETE RESTRICT,
  ADD CONSTRAINT fk_instruction_formula
    FOREIGN KEY (formula_id) REFERENCES formulas (id) ON DELETE RESTRICT;

ALTER TABLE parameters
  ADD CONSTRAINT fk_parameter_instruction
    FOREIGN KEY (freeze_dry_instruction_id) REFERENCES freeze_dry_instructions (id) ON DELETE CASCADE;

-- ---------------------------------------------------------------------
-- Startgegevens
--
-- Let op: de acceptatietest maakt deze tabellen leeg en zet er zijn eigen
-- gegevens in. Zie Testdata in src/test/java.
-- ---------------------------------------------------------------------
INSERT INTO bio_engineers (id, name, email, phone_number, specialization, password) VALUES
  (1, 'Dr. Sarah Johnson', 'sarah.johnson@lifeisbananas.com', '+32-123-456-789', 'Fruit Processing', 'banaan123'),
  (2, 'John Doe', 'john.doe@lifeisbananas.com', NULL, 'Freeze-Drying Expert', 'banaan123');

INSERT INTO nutritional_values (id, calories, proteins, carbohydrates, fats, fibers, sugars) VALUES
  (1, 89.0, 1.1, 22.8, 0.3, 2.6, 12.2),
  (2, 32.0, 0.7, 7.7, 0.3, 2.0, 4.9),
  (3, 23.0, 2.9, 3.6, 0.4, 2.2, 0.4);

INSERT INTO raw_materials (id, material_type, name, organic_certified, price, unit, nutritional_value_id,
                           ripeness_level, sugar_content, acidity_level, firmness, moisture_content) VALUES
  (1, 'FRUIT', 'Banana', b'1', 2.50, 'kg', 1, 'rijp', 12.2, 4.5, NULL, NULL),
  (2, 'FRUIT', 'Strawberry', b'1', 6.80, 'kg', 2, 'rijp', 4.9, 3.4, NULL, NULL),
  (3, 'VEGETABLE', 'Spinach', b'0', 3.20, 'kg', 3, NULL, NULL, NULL, 'stevig', 91.4);

INSERT INTO formulas (id, name, version, preparation_method, creation_date, last_modified, status, bio_engineer_id) VALUES
  (1, 'Banana Chips Formula', '1.0',
   '1. Was de bananen. 2. Snijd ze op 3 mm. 3. Vries in op -40 graden. 4. Droog 24 uur.',
   '2025-04-15', '2025-04-15', 'IN_DEVELOPMENT', 1),
  (2, 'Green Smoothie Mix', '2.1',
   '1. Was de spinazie. 2. Meng met aardbei. 3. Vries in. 4. Droog 18 uur.',
   '2025-05-02', '2025-05-02', 'IN_DEVELOPMENT', 1);

INSERT INTO ingredients (id, quantity, unit, sequence, formula_id, raw_material_id) VALUES
  (1, 200.00, 'GRAM', 1, 1, 2),
  (2, 250.00, 'GRAM', 1, 2, 3),
  (3, 100.00, 'GRAM', 2, 2, 2);
