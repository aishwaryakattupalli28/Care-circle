import abc
from typing import Dict, Any, Optional
from app.core.logging import logger

class NotificationChannel(abc.ABC):
    @abc.abstractmethod
    async def send(self, recipient: str, title: str, body: str, data: Optional[Dict[str, Any]] = None) -> bool:
        pass

class MockExpoPushService(NotificationChannel):
    async def send(self, recipient: str, title: str, body: str, data: Optional[Dict[str, Any]] = None) -> bool:
        logger.info(f"[PUSH SENT via EXPO] To: {recipient} | Title: {title} | Body: {body} | Data: {data}")
        return True

class MockTelegramBotService(NotificationChannel):
    async def send(self, recipient: str, title: str, body: str, data: Optional[Dict[str, Any]] = None) -> bool:
        logger.info(f"[TELEGRAM ESCALATION] ChatId: {recipient} | Message: 🚨 {title}: {body}")
        return True

class MockTwilioSmsCallService(NotificationChannel):
    async def send(self, recipient: str, title: str, body: str, data: Optional[Dict[str, Any]] = None) -> bool:
        channel_type = data.get("call_type", "sms") if data else "sms"
        if channel_type == "voice":
            logger.info(f"[TWILIO VOICE CALL ESCALATION] Dialing: {recipient} | Automated Text-To-Speech: 'Urgent CareCircle Alert: {title}. {body}'")
        else:
            logger.info(f"[TWILIO SMS ESCALATION] To: {recipient} | Text: 'URGENT CareCircle Alert: {title} - {body}'")
        return True

expo_service = MockExpoPushService()
telegram_service = MockTelegramBotService()
twilio_service = MockTwilioSmsCallService()
