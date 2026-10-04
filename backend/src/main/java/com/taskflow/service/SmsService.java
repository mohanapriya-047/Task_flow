package com.taskflow.service;

public interface SmsService {
    void sendSms(String phoneNumber, String message);
}
