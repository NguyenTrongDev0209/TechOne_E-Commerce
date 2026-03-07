package com.techone.service.impl;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.techone.service.MailerService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailerServiceImpl implements MailerService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void send(String to, String subject, String body) throws MessagingException {
        this.send(to, subject, body, new String[0]);
    }

    @Override
    public void send(String to, String subject, String body, String... attachments) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.setFrom("TechOne <nguyentrongdev0209@gmail.com>");
        
        for (String path : attachments) {
            File file = new File(path);
            helper.addAttachment(file.getName(), file);
        }
        
        mailSender.send(message);
    }
}
