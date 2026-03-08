package com.techone.common.service;

import jakarta.mail.MessagingException;

public interface MailerService {
    void send(String to, String subject, String body) throws MessagingException;

    void send(String to, String subject, String body, String... attachments) throws MessagingException;
}
