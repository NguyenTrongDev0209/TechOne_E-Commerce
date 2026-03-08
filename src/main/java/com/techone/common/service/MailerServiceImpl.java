package com.techone.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
@RequiredArgsConstructor
public class MailerServiceImpl implements MailerService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void send(String to, String subject, String body) throws MessagingException {
        this.send(to, subject, body, new String[0]);
    }

    @Override
    @Async
    public void send(String to, String subject, String body, String... attachments) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.setFrom("TechOne <techone.ecommerce@gmail.com>");

        if (attachments != null && attachments.length > 0) {
            for (String path : attachments) {
                File file = new File(path);
                helper.addAttachment(file.getName(), file);
            }
        }
        mailSender.send(message);
    }
}
