package com.taskflow.util;

import org.springframework.stereotype.Component;

@Component
public class MaskingUtil {

    /**
     * mohanapriya@gmail.com -> m***@gmail.com
     * Never used to derive a value stored back in place of the real email -
     * masking is strictly a display-time transformation.
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        String maskedLocal = local.length() <= 1
                ? local + "***"
                : local.charAt(0) + "***";
        return maskedLocal + "@" + domain;
    }

    /** +919876543210 -> +9198*****10 */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        int visibleStart = 4;
        int visibleEnd = 2;
        String start = phone.substring(0, visibleStart);
        String end = phone.substring(phone.length() - visibleEnd);
        return start + "*".repeat(Math.max(3, phone.length() - visibleStart - visibleEnd)) + end;
    }
}
