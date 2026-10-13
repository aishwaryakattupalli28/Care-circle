# CareCircle Backend Service

> **Medical Disclaimer**: CareCircle is an observational remote monitoring aid intended for informational and caregiver awareness purposes only. It is **NOT** an FDA/CE-cleared diagnostic medical device and must never replace clinical judgement, professional medical evaluation, or emergency services (e.g. 911).

CareCircle is a robust, asynchronous backend for remote patient vital-sign monitoring. It powers both real-time patient companion apps and caregiver dashboards with consent-enforced Row Level Security (RLS), multi-source telemetry deduplication, live data freshness classification, threshold breach engines, and tiered escalation ladders.

---

## 1. System Architecture & Folder Tree

```
carecircle-backend/
├── Dockerfile                   # Production Python 3.11 container definition
├── requirements.txt             # FastAPI, Pydantic v2, Uvicorn, Supabase, WebSockets
├── .env.example                 # Environment configuration template
├── README.md                    # Architecture, setup, API contracts & documentation
├── migrations/
│   └── 001_initial_schema.sql   # Supabase Postgres tables, indexes & RLS policies
├── app/
│   ├── main.py                  # FastAPI application entrypoint & lifespan
│   ├── core/
│   │   ├── config.py            # Pydantic v2 application settings
│   │   └── logging.py           # Structured JSON logger
│   ├── models/
│   │   └── enums.py             # UserRole, VitalMetric, QualityStatus, AlertType, Freshness
│   ├── schemas/
│   │   ├── vitals.py            # VitalBatchIngestRequest, VitalReadingResponse, History
│   │   ├── alerts.py            # AlertResponse, ThresholdRuleList, ManualSOSRequest
│   │   └── profile.py           # Profiles, CareLink consent, and Source requests
│   ├── services/
│   │   ├── freshness.py         # Read-time freshness matrix calculation (live, delayed, stale)
│   │   ├── normalizer.py        # Physiological bounds & canonical unit conversion
│   │   ├── rules_engine.py      # Threshold evaluation & escalation ladder dispatcher
│   │   ├── websocket_manager.py # Realtime multi-client WebSocket connection manager
│   │   ├── store.py             # Thread-safe async data store with demo seed data
│   │   └── simulator.py         # Scenario telemetry generator (tachycardia, fever, etc.)
│   ├── workers/
│   │   └── stale_monitor.py     # 15s periodic background detector for stale vitals & disconnects
│   ├── integrations/
│   │   └── notifications.py     # Pluggable Expo Push, Telegram Bot & Twilio SMS/Voice channels
│   └── api/
│       ├── deps.py              # JWT authentication & Caregiver RLS consent verification
│       └── v1/
│           ├── auth.py          # /me, /me/push-token
│           ├── care_links.py    # /care-links/invite, /care-links/{id}/accept, revoke
│           ├── sources.py       # /sources device management
│           ├── vitals.py        # /vitals/batch, /patients/{id}/vitals/latest & history
│           ├── alerts.py        # /patients/{id}/alerts, acknowledge, resolve, /sos
│           ├── sim.py           # /sim/start, /sim/stop scenarios
│           └── websocket.py     # /ws/patients/{id} live telemetry stream
└── tests/
    └── test_carecircle.py       # Pytest suite for freshness, deduplication & rules
```

---

## 2. Core Principles & Logic

1. **Never present delayed data as live**:
   - Freshness is computed dynamically at **read time**, never hardcoded at storage time.
   - Readings are classified as **Live** (age <= live limit AND source is BLE or simulator), **Delayed** (age <= delayed limit), or **Stale** (older).
   - Health Connect, HealthKit, and manual entries are *never* presented as live.
2. **One row per vital reading**:
   - Smartwatches deliver heart rate, SpO2, and temperature at different sampling frequencies. Each metric is ingested as an independent observation.
3. **Idempotency & Cross-Source Conflict Resolution**:
   - Deduplicated on `(patient_id, metric, source_id, source_record_id)` for zero-loss offline retries.
   - If two sources report the same metric within 10 seconds, the source with higher priority is retained, and the lower priority is marked `is_suppressed=true`.
4. **Physiological Bounds & Unit Normalization**:
   - Readings in °F or kPa are normalized to standard units (°C, mmHg, bpm, %).
   - Impossible values (e.g. Heart Rate 400 bpm) are flagged `quality_status = invalid`.
5. **Caregiver Access Consent (RLS)**:
   - Data access requires an explicit `care_links` entry with `status = 'accepted'`. All accesses are audited in `caregiver_access_audit`.
6. **Tiered Escalation Ladder**:
   - **Level 0**: Targeted Push notification to Primary Caregiver via Expo Push.
   - **Level 1** (unacknowledged for 3 mins): Broadcast Push + Telegram Bot alert to all caregivers.
   - **Level 2** (unacknowledged for 6 mins): Twilio SMS + Automated Voice Call.
   - `POST /v1/alerts/{id}/acknowledge` stops the escalation ladder immediately.

---

## 3. Quickstart & Deployment

### Local Development
```bash
cd carecircle-backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### Docker Deployment
```bash
docker build -t carecircle-backend:latest .
docker run -p 8000:8000 --env-file .env.example carecircle-backend:latest
```

### Running Tests
```bash
pytest -v tests/
```

---

## 4. Frontend Contract (Expo / React Native Integration)

### 1. Ingestion: `POST /v1/vitals/batch`
**Request**:
```json
{
  "readings": [
    {
      "metric": "heart_rate",
      "value": 74.0,
      "unit": "bpm",
      "measured_at": "2026-10-12T10:00:00Z",
      "source_id": "44444444-4444-4444-4444-444444444444",
      "source_record_id": "watch_hr_1001",
      "sync_mode": "live",
      "patient_id": "11111111-1111-1111-1111-111111111111"
    }
  ]
}
```
**Response** (200 OK):
```json
[
  {
    "id": "e4f8a123-b1c2-4982-a9b1-591a78912345",
    "patient_id": "11111111-1111-1111-1111-111111111111",
    "metric": "heart_rate",
    "value": 74.0,
    "canonical_value": 74.0,
    "unit": "bpm",
    "canonical_unit": "bpm",
    "measured_at": "2026-10-12T10:00:00Z",
    "received_at": "2026-10-12T10:00:01Z",
    "source_id": "44444444-4444-4444-4444-444444444444",
    "source_kind": "ble",
    "sync_mode": "live",
    "quality_status": "valid",
    "freshness": "live",
    "age_seconds": 1.2,
    "is_suppressed": false
  }
]
```

### 2. Latest Vitals: `GET /v1/patients/{id}/vitals/latest`
**Response** (200 OK):
```json
{
  "patient_id": "11111111-1111-1111-1111-111111111111",
  "computed_at": "2026-10-12T10:05:00Z",
  "vitals": [
    {
      "id": "e4f8a123-b1c2-4982-a9b1-591a78912345",
      "metric": "heart_rate",
      "canonical_value": 74.0,
      "canonical_unit": "bpm",
      "measured_at": "2026-10-12T10:04:55Z",
      "freshness": "live",
      "age_seconds": 5.0,
      "source_kind": "ble",
      "quality_status": "valid"
    },
    {
      "id": "f5e9b234-c2d3-4a93-bac2-602b89023456",
      "metric": "spo2",
      "canonical_value": 98.0,
      "canonical_unit": "%",
      "measured_at": "2026-10-12T10:04:50Z",
      "freshness": "live",
      "age_seconds": 10.0,
      "source_kind": "ble",
      "quality_status": "valid"
    }
  ]
}
```

### 3. Acknowledge Alert: `POST /v1/alerts/{id}/acknowledge`
**Request**:
```json
{
  "note": "Spoke with patient; administering rescue inhaler."
}
```
**Response** (200 OK):
```json
{
  "id": "c1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c",
  "patient_id": "11111111-1111-1111-1111-111111111111",
  "type": "threshold",
  "metric": "heart_rate",
  "severity": "critical",
  "status": "acknowledged",
  "trigger_message": "heart_rate high: 142.0bpm (max threshold: 110.0)",
  "acknowledged_by": "22222222-2222-2222-2222-222222222222",
  "acknowledged_at": "2026-10-12T10:06:12Z",
  "escalation_level": 0
}
```

### 4. Manual SOS: `POST /v1/sos`
**Request**:
```json
{
  "patient_id": "11111111-1111-1111-1111-111111111111",
  "note": "Patient pressed emergency SOS on watch"
}
```

### 5. Simulator Control: `POST /v1/sim/start`
**Request**:
```json
{
  "patient_id": "11111111-1111-1111-1111-111111111111",
  "scenario": "tachycardia"
}
```

### 6. WebSocket Live Stream: `ws://localhost:8000/v1/ws/patients/{id}`
Receives live frames:
```json
{
  "type": "VITAL_READING",
  "patient_id": "11111111-1111-1111-1111-111111111111",
  "data": {
    "metric": "heart_rate",
    "canonical_value": 142.0,
    "canonical_unit": "bpm",
    "freshness": "live",
    "age_seconds": 0.5,
    "quality_status": "valid"
  }
}
```
