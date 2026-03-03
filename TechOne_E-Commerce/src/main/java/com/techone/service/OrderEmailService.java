package com.techone.service;

import com.techone.model.Order;
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

            int detailCount = (order.getOrderDetail() != null) ? order.getOrderDetail().size() : 0;
            System.out.println("DEBUG: Sending invoice for Order #" + order.getId() + " - Products: " + detailCount);

            String process = templateEngine.process("email/order-invoice", context);

            String emailTo = order.getAccount().getEmail();
            if (emailTo != null && !emailTo.isEmpty()) {
                mailerService.send(emailTo, "TechOne - Hóa đơn đơn hàng #" + order.getId(), process);
                System.out.println("DEBUG: Invoice email sent to " + emailTo + " for Order #" + order.getId());
            } else {
                System.err.println("DEBUG: Cannot send email, user " + order.getAccount().getFullname()
                        + " has no email address.");
            }
        } catch (MessagingException e) {
            System.err
                    .println("DEBUG: Error sending invoice email for Order #" + order.getId() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("DEBUG: Unexpected error in sendOrderInvoice: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
