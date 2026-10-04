package com.taskflow.service.impl;

import com.taskflow.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Placeholder Twilio-backed implementation. Add the Twilio SDK dependency and
 * fill in the API call to activate; kept minimal here so the module compiles
 * without requiring the Twilio dependency by default.
 */
@Service
@ConditionalOnProperty(name = "taskflow.sms.provider", havingValue = "twilio")
public class TwilioSmsService implements SmsService {

    @Value("${taskflow.sms.api-key}")
    private String apiKey;

    @Value("${taskflow.sms.api-secret}")
    private String apiSecret;

    @Value("${taskflow.sms.from-number}")
    private String fromNumber;

    @Override
    public void sendSms(String phoneNumber, String message) {
        // TODO: wire up com.twilio.Twilio / MessageCreator here using apiKey/apiSecret/fromNumber.
        // Left unimplemented intentionally: do not hard-code credentials or send real
        // SMS from a scaffold. See TwilioSmsService for where to add the SDK call.
        throw new UnsupportedOperationException(
                "Twilio SMS provider is not wired up yet. Implement TwilioSmsService.sendSms(...).");
    }
}
