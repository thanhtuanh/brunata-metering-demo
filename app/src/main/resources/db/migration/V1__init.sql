CREATE TABLE IF NOT EXISTS device (
  id UUID PRIMARY KEY,
  type VARCHAR(40),
  serial_no VARCHAR(80) UNIQUE NOT NULL,
  location TEXT,
  last_seen_at TIMESTAMPTZ,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
CREATE TABLE IF NOT EXISTS meter_reading (
  id UUID PRIMARY KEY,
  device_id UUID NOT NULL,
  reading_time TIMESTAMPTZ NOT NULL,
  value NUMERIC(18,6) NOT NULL CHECK (value >= 0),
  unit VARCHAR(16) NOT NULL,
  source VARCHAR(16) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_reading_device_time ON meter_reading(device_id, reading_time);
