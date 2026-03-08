package com.techone.domain.order.service;

import com.techone.domain.order.entity.Order;
import com.techone.common.service.MailerService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class OrderEmailService {
    private final MailerService mailerService;
    private final TemplateEngine templateEngine;

    public void sendOrderInvoice(Order order) {
        try {
            Context context = new Context();
            context.setVariable("order", order);
            String process = templateEngine.process("email/order-invoice", context);
            String emailTo = order.getAccount().getEmail();
            if (emailTo != null && !emailTo.isEmpty()) {
                mailerService.send(emailTo, "TechOne - Hóa đơn đơn hàng #" + order.getId(), process);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

