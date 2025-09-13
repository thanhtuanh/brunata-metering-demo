-- V5__demo_seed.sql
-- Demo-Seed-Daten: ein Tarif, ein Gerät, ein Vertrag und zwei Messwerte
-- Feste UUIDs, damit README/Beispiele stabil bleiben

-- Tariff
INSERT INTO tariff (id, name, price_per_unit, unit)
VALUES ('00000000-0000-0000-0000-000000000001', 'Standard', 0.2500, 'kWh');

-- Device
INSERT INTO device (id, type, serial_no, location, last_seen_at, status)
VALUES ('62eb5088-15b6-4128-b7fe-44690e42099d', 'HEAT', 'DEMO-0001', 'Demo', now(), 'ACTIVE');

-- Contract
INSERT INTO contract (id, customer_name, device_id, start_date, end_date, tariff_id)
VALUES (
  'f70ca20d-a2f3-4d87-ab64-482fe327d4c4',
  'Musterkunde',
  '62eb5088-15b6-4128-b7fe-44690e42099d',
  current_date,
  NULL,
  '00000000-0000-0000-0000-000000000001'
);

-- Readings: Tag 10 und 25 des aktuellen Monats, 00:00:00 UTC
INSERT INTO meter_reading (id, device_id, reading_time, value, unit, source)
VALUES
 ('58ca87f0-dc07-4f3e-b001-a23b6bfe2721',
  '62eb5088-15b6-4128-b7fe-44690e42099d',
  ((date_trunc('month', now()) + interval '9 day')::date)::timestamptz,
  100.000000,
  'kWh',
  'Demo'),
 ('a1bb8458-753e-49bf-8456-78916f00000e',
  '62eb5088-15b6-4128-b7fe-44690e42099d',
  ((date_trunc('month', now()) + interval '24 day')::date)::timestamptz,
  160.500000,
  'kWh',
  'Demo');

