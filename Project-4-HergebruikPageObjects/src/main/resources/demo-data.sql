-- Startgegevens voor het demoprofiel (H2). Het schema bouwt Hibernate zelf op.
--
-- Omdat hier vaste id's gebruikt worden, moet de teller van elke
-- identity-kolom daarna verder gezet worden. Anders probeert Hibernate bij
-- de eerste nieuwe rij opnieuw id 1 en botst ze op de bestaande rij.

INSERT INTO bio_engineers (id, name, email, phone_number, specialization, password) VALUES
  (1, 'Dr. Sarah Johnson', 'sarah.johnson@lifeisbananas.com', '+32-123-456-789', 'Fruit Processing', 'banaan123'),
  (2, 'John Doe', 'john.doe@lifeisbananas.com', NULL, 'Freeze-Drying Expert', 'banaan123');

INSERT INTO nutritional_values
  (id, calories, proteins, carbohydrates, fats, fibers, sugars, salt, vitamin_a, vitamin_c, calcium, iron) VALUES
  (1, 89.0, 1.1, 22.8, 0.3, 2.6, 12.2, 0.0, 3.0, 8.7, 5.0, 0.26),
  (2, 32.0, 0.7, 7.7, 0.3, 2.0, 4.9, 0.0, 1.0, 58.8, 16.0, 0.41),
  (3, 23.0, 2.9, 3.6, 0.4, 2.2, 0.4, 0.08, 469.0, 28.1, 99.0, 2.71);

INSERT INTO raw_materials (id, material_type, name, organic_certified, price, unit, nutritional_value_id,
                           ripeness_level, sugar_content, acidity_level, firmness, moisture_content) VALUES
  (1, 'FRUIT', 'Banana', TRUE, 2.50, 'kg', 1, 'rijp', 12.2, 4.5, NULL, NULL),
  (2, 'FRUIT', 'Strawberry', TRUE, 6.80, 'kg', 2, 'rijp', 4.9, 3.4, NULL, NULL),
  (3, 'VEGETABLE', 'Spinach', FALSE, 3.20, 'kg', 3, NULL, NULL, NULL, 'stevig', 91.4);

INSERT INTO formulas (id, name, version, preparation_method, creation_date, last_modified, status, bio_engineer_id) VALUES
  (1, 'Banana Chips Formula', '1.0',
   '1. Was de bananen. 2. Snijd ze op 3 mm. 3. Vries in op -40 graden. 4. Droog 24 uur.',
   DATE '2025-04-15', DATE '2025-04-15', 'IN_DEVELOPMENT', 1),
  (2, 'Green Smoothie Mix', '2.1',
   '1. Was de spinazie. 2. Meng met aardbei. 3. Vries in. 4. Droog 18 uur.',
   DATE '2025-05-02', DATE '2025-05-02', 'IN_DEVELOPMENT', 1);

INSERT INTO ingredients (id, quantity, unit, sequence, formula_id, raw_material_id) VALUES
  (1, 200.00, 'GRAM', 1, 1, 2),
  (2, 250.00, 'GRAM', 1, 2, 3),
  (3, 100.00, 'GRAM', 2, 2, 2);

ALTER TABLE bio_engineers ALTER COLUMN id RESTART WITH 100;
ALTER TABLE nutritional_values ALTER COLUMN id RESTART WITH 100;
ALTER TABLE raw_materials ALTER COLUMN id RESTART WITH 100;
ALTER TABLE formulas ALTER COLUMN id RESTART WITH 100;
ALTER TABLE ingredients ALTER COLUMN id RESTART WITH 100;
