-- V7__device_indexes.sql
-- Indizes zur Beschleunigung von Offline-Checks im Sync-Service

-- Index auf last_seen_at für Suchen nach älteren/fehlenden Zeitpunkten
CREATE INDEX IF NOT EXISTS idx_device_last_seen_at ON device(last_seen_at);

-- Optional: kombinierter Index für häufige Filter (Status + Last Seen)
CREATE INDEX IF NOT EXISTS idx_device_status_last_seen ON device(status, last_seen_at);

