-- V4__invoice_constraints.sql
-- Zusätzliche Datenqualitätsregeln für Rechnungen

ALTER TABLE invoice
    ADD CONSTRAINT chk_invoice_amount_nonneg CHECK (amount >= 0),
    ADD CONSTRAINT chk_invoice_consumption_nonneg CHECK (consumption >= 0),
    ADD CONSTRAINT chk_invoice_status_enum CHECK (status IN ('OPEN','PAID','VOID'));
