-- V6__demo_seed_fix_past.sql
-- Korrigiert Seed-Daten: stellt sicher, dass die beiden Demo-Messwerte in der Vergangenheit liegen,
-- damit neue Messwerte (Monotonie + @PastOrPresent) praktikabel sind.

-- Älterer Messwert: jetzt - 3 Tage (00:00 UTC)
UPDATE meter_reading
SET reading_time = ((date_trunc('day', now() - interval '3 day'))::timestamptz)
WHERE id = '58ca87f0-dc07-4f3e-b001-a23b6bfe2721';

-- Neuerer Messwert: jetzt - 1 Tag (00:00 UTC)
UPDATE meter_reading
SET reading_time = ((date_trunc('day', now() - interval '1 day'))::timestamptz)
WHERE id = 'a1bb8458-753e-49bf-8456-78916f00000e';

