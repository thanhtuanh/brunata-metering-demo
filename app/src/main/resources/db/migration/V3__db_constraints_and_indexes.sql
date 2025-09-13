-- V3__db_constraints_and_indexes.sql
-- Ziel: Datenqualitäts-Constraints und sinnvolle Indizes (PostgreSQL)

-- Messwerte: Werte >= 0, Einheit nicht leer
ALTER TABLE meter_reading
    ADD CONSTRAINT chk_meter_reading_value_nonneg CHECK (value >= 0),
    ADD CONSTRAINT chk_meter_reading_unit_not_empty CHECK (unit IS NOT NULL AND length(trim(unit)) > 0);

-- Schneller Zugriff: neuester Messwert pro Gerät
CREATE INDEX IF NOT EXISTS idx_reading_device_time_desc ON meter_reading(device_id, reading_time DESC);

-- Rechnung: keine doppelten Perioden pro Vertrag
ALTER TABLE invoice
    ADD CONSTRAINT uq_invoice_contract_period UNIQUE (contract_id, period_from, period_to);

-- Partial Index für offene Rechnungen
CREATE INDEX IF NOT EXISTS idx_invoice_open ON invoice(contract_id) WHERE status = 'OPEN';

-- Dokumentation
COMMENT ON TABLE meter_reading IS 'Stores cumulative meter readings for devices.';
COMMENT ON COLUMN meter_reading.value IS 'Cumulative reading value; expected to be non-decreasing per device.';
