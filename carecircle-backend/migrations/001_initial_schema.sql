-- CareCircle Postgres Migration & Schema Setup
-- Supabase Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enums
CREATE TYPE user_role AS ENUM ('patient', 'caregiver');
CREATE TYPE care_link_status AS ENUM ('pending', 'accepted', 'revoked');
CREATE TYPE source_kind AS ENUM ('simulator', 'ble', 'health_connect', 'healthkit', 'manual');
CREATE TYPE vital_metric AS ENUM ('heart_rate', 'spo2', 'body_temperature', 'blood_pressure_systolic', 'blood_pressure_diastolic', 'respiratory_rate');
CREATE TYPE sync_mode AS ENUM ('live', 'periodic', 'manual');
CREATE TYPE quality_status AS ENUM ('valid', 'suspect', 'invalid');
CREATE TYPE alert_type AS ENUM ('threshold', 'stale_data', 'device_disconnected', 'manual_sos');
CREATE TYPE alert_severity AS ENUM ('info', 'warning', 'critical');
CREATE TYPE alert_status AS ENUM ('open', 'acknowledged', 'escalated', 'resolved');

-- 1. Profiles Table
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    role user_role NOT NULL,
    name TEXT NOT NULL,
    phone TEXT,
    telegram_chat_id TEXT,
    expo_push_token TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 2. Care Links Table (Consent-based links)
CREATE TABLE IF NOT EXISTS care_links (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    caregiver_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    status care_link_status NOT NULL DEFAULT 'pending',
    priority_order INT NOT NULL DEFAULT 1,
    consented_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    UNIQUE(patient_id, caregiver_id)
);

-- 3. Sources Table (Tracking wearable/device sensors)
CREATE TABLE IF NOT EXISTS sources (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    kind source_kind NOT NULL,
    device_name TEXT NOT NULL,
    priority INT NOT NULL DEFAULT 10,
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 4. Vitals Table
CREATE TABLE IF NOT EXISTS vitals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    metric vital_metric NOT NULL,
    value NUMERIC(8, 2) NOT NULL,
    unit TEXT NOT NULL,
    measured_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    source_id UUID NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
    source_record_id TEXT NOT NULL,
    sync_mode sync_mode NOT NULL,
    quality_status quality_status NOT NULL DEFAULT 'valid',
    is_suppressed BOOLEAN NOT NULL DEFAULT FALSE,
    raw_payload JSONB DEFAULT '{}'::jsonb,
    CONSTRAINT uq_patient_metric_source_record UNIQUE(patient_id, metric, source_id, source_record_id)
);

CREATE INDEX IF NOT EXISTS idx_vitals_patient_metric_time ON vitals(patient_id, metric, measured_at DESC);

-- 5. Threshold Rules Table
CREATE TABLE IF NOT EXISTS threshold_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    metric vital_metric NOT NULL,
    min_value NUMERIC(8, 2),
    max_value NUMERIC(8, 2),
    severity alert_severity NOT NULL DEFAULT 'warning',
    sustained_seconds INT NOT NULL DEFAULT 30,
    max_age_seconds INT NOT NULL DEFAULT 120,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_patient_metric_rule UNIQUE(patient_id, metric)
);

-- 6. Freshness Policies Table
CREATE TABLE IF NOT EXISTS freshness_policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric vital_metric NOT NULL,
    source_kind source_kind NOT NULL,
    live_max_age_s INT NOT NULL,
    delayed_max_age_s INT NOT NULL,
    CONSTRAINT uq_metric_source_kind UNIQUE(metric, source_kind)
);

-- 7. Alerts Table
CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    type alert_type NOT NULL,
    metric vital_metric,
    severity alert_severity NOT NULL,
    status alert_status NOT NULL DEFAULT 'open',
    trigger_message TEXT NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    acknowledged_by UUID REFERENCES profiles(id),
    acknowledged_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,
    escalation_level INT NOT NULL DEFAULT 0,
    last_escalated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE INDEX IF NOT EXISTS idx_alerts_patient_open ON alerts(patient_id, status) WHERE status IN ('open', 'escalated');

-- 8. Alert Events Table (Audit Log)
CREATE TABLE IF NOT EXISTS alert_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    alert_id UUID NOT NULL REFERENCES alerts(id) ON DELETE CASCADE,
    event_type TEXT NOT NULL,
    channel TEXT,
    recipient_id UUID REFERENCES profiles(id),
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 9. Caregiver Access Audit Log
CREATE TABLE IF NOT EXISTS caregiver_access_audit (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    caregiver_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    patient_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    endpoint TEXT NOT NULL,
    accessed_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Row Level Security (RLS) Policies
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE care_links ENABLE ROW LEVEL SECURITY;
ALTER TABLE sources ENABLE ROW LEVEL SECURITY;
ALTER TABLE vitals ENABLE ROW LEVEL SECURITY;
ALTER TABLE threshold_rules ENABLE ROW LEVEL SECURITY;
ALTER TABLE alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE alert_events ENABLE ROW LEVEL SECURITY;

-- Helper functions
CREATE OR REPLACE FUNCTION is_caregiver_for_patient(p_patient_id UUID, p_caregiver_id UUID)
RETURNS BOOLEAN AS 304
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM care_links 
        WHERE patient_id = p_patient_id 
          AND caregiver_id = p_caregiver_id 
          AND status = 'accepted'
    );
END;
304 LANGUAGE plpgsql SECURITY DEFINER;

-- Profiles: Users see themselves and linked accepted patients/caregivers
CREATE POLICY profile_self ON profiles FOR ALL USING (auth.uid() = id);
CREATE POLICY profile_caregiver_view ON profiles FOR SELECT USING (
    is_caregiver_for_patient(id, auth.uid()) OR is_caregiver_for_patient(auth.uid(), id)
);

-- Care Links
CREATE POLICY care_links_patient ON care_links FOR ALL USING (auth.uid() = patient_id);
CREATE POLICY care_links_caregiver ON care_links FOR ALL USING (auth.uid() = caregiver_id);

-- Sources
CREATE POLICY sources_patient ON sources FOR ALL USING (auth.uid() = patient_id);
CREATE POLICY sources_caregiver ON sources FOR SELECT USING (is_caregiver_for_patient(patient_id, auth.uid()));

-- Vitals
CREATE POLICY vitals_patient ON vitals FOR ALL USING (auth.uid() = patient_id);
CREATE POLICY vitals_caregiver ON vitals FOR SELECT USING (is_caregiver_for_patient(patient_id, auth.uid()));

-- Threshold Rules
CREATE POLICY threshold_patient ON threshold_rules FOR ALL USING (auth.uid() = patient_id);
CREATE POLICY threshold_caregiver ON threshold_rules FOR ALL USING (is_caregiver_for_patient(patient_id, auth.uid()));

-- Alerts
CREATE POLICY alerts_patient ON alerts FOR ALL USING (auth.uid() = patient_id);
CREATE POLICY alerts_caregiver ON alerts FOR ALL USING (is_caregiver_for_patient(patient_id, auth.uid()));

-- Default Freshness Policies Seeding
INSERT INTO freshness_policies (metric, source_kind, live_max_age_s, delayed_max_age_s) VALUES
('heart_rate', 'ble', 10, 900),
('heart_rate', 'simulator', 10, 900),
('heart_rate', 'health_connect', 0, 1800),
('heart_rate', 'healthkit', 0, 1800),
('heart_rate', 'manual', 0, 3600),
('spo2', 'ble', 30, 1800),
('spo2', 'simulator', 30, 1800),
('spo2', 'health_connect', 0, 3600),
('spo2', 'healthkit', 0, 3600),
('spo2', 'manual', 0, 7200),
('body_temperature', 'ble', 60, 1800),
('body_temperature', 'simulator', 60, 1800),
('body_temperature', 'manual', 0, 14400),
('blood_pressure_systolic', 'ble', 60, 3600),
('blood_pressure_systolic', 'simulator', 60, 3600),
('blood_pressure_systolic', 'manual', 0, 21600),
('blood_pressure_diastolic', 'ble', 60, 3600),
('blood_pressure_diastolic', 'simulator', 60, 3600),
('blood_pressure_diastolic', 'manual', 0, 21600),
('respiratory_rate', 'ble', 30, 1800),
('respiratory_rate', 'simulator', 30, 1800),
('respiratory_rate', 'manual', 0, 7200)
ON CONFLICT (metric, source_kind) DO NOTHING;
