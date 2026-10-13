from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    PROJECT_NAME: str = "CareCircle Backend"
    VERSION: str = "1.0.0"
    ENVIRONMENT: str = "development"
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    LOG_LEVEL: str = "INFO"
    
    SUPABASE_URL: str = "https://demo.supabase.co"
    SUPABASE_KEY: str = "demo-anon-key"
    SUPABASE_JWT_SECRET: str = "super-secret-jwt-token-with-minimum-32-chars-long!"
    
    USE_IN_MEMORY_FALLBACK: bool = True
    
    EXPO_PUSH_ACCESS_TOKEN: Optional[str] = None
    TELEGRAM_BOT_TOKEN: Optional[str] = None
    TWILIO_ACCOUNT_SID: Optional[str] = None
    TWILIO_AUTH_TOKEN: Optional[str] = None
    TWILIO_FROM_PHONE: Optional[str] = None
    
    BACKGROUND_TICK_SECONDS: int = 15
    STALE_DETECTION_INTERVAL_SECONDS: int = 15
    
    # Escalation default timeouts (minutes)
    ESCALATION_LEVEL_1_MINUTES: int = 3
    ESCALATION_LEVEL_2_MINUTES: int = 6

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        extra = "ignore"

settings = Settings()
