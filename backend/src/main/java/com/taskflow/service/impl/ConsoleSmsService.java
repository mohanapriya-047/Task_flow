package com.taskflow.service.impl;

import com.taskflow.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Default SmsService: logs the message instead of sending a real SMS.
 * Swap in TwilioSmsService (or another provider) by setting
 * taskflow.sms.provider=twilio and the SMS_API_KEY/SMS_API_SECRET/SMS_PHONE_NUMBER env vars.
 */
@Service
@ConditionalOnProperty(name = "taskflow.sms.provider", havingValue = "console", matchIfMissing = true)
@Slf4j
public class ConsoleSmsService implements SmsService {

    @Override
    public void sendSms(String phoneNumber, String message) {
        log.info("[DEV SMS] To: {} | Message: {}", phoneNumber, message);
    }
}
