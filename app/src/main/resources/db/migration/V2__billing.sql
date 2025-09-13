-- V2__billing.sql
-- Billing-Grundtabellen: Tarif, Vertrag, Rechnung
-- Gestaltung: einfache FK-Referenzen, numerische Genauigkeit für Geld-/Mengenfelder
-- Tarife (Preis je Einheit, z. B. kWh)
CREATE TABLE IF NOT EXISTS tariff (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL,
  price_per_unit NUMERIC(12,4) NOT NULL,
  unit VARCHAR(16) NOT NULL
);

-- Vertrag referenziert einen Tarif und ein Gerät (device_id)
CREATE TABLE IF NOT EXISTS contract (
  id UUID PRIMARY KEY,
  customer_name TEXT NOT NULL,
  device_id UUID NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE,
  tariff_id UUID NOT NULL REFERENCES tariff(id)
);

-- Rechnung enthält Abrechnungszeitraum, Verbrauch und Betrag
CREATE TABLE IF NOT EXISTS invoice (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL REFERENCES contract(id),
  period_from DATE NOT NULL,
  period_to DATE NOT NULL,
  consumption NUMERIC(18,6) NOT NULL,
  amount NUMERIC(18,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index zur schnellen Abfrage nach Vertrag
CREATE INDEX IF NOT EXISTS idx_invoice_contract ON invoice(contract_id);
