package com.ordermonitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ordermonitor.entity.Order;
import com.ordermonitor.entity.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Sends HTML email notifications.
 *
 * All emails come from: online.monitor.apt@gmail.com
 * All send methods are @Async – they never block the HTTP thread.
 *
 * Bug fixes applied:
 *  1. MimeMessageHelper multipart=false  → correct for HTML-only emails (no attachments).
 *     multipart=true wraps the body in multipart/mixed, causing Gmail SMTP to silently
 *     drop or misrender the message.
 *  2. spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com must be set in
 *     application.properties so the STARTTLS handshake on port 587 succeeds reliably.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:online.monitor.apt@gmail.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ---------------------------------------------------------------
    // Customer Emails
    // ---------------------------------------------------------------

    @Async("emailExecutor")
    public void sendOrderPlacedEmail(User user, Order order) {
        String subject = "📋 Order Placed – #" + order.getId() + " | " + order.getProductName();
        String body = buildOrderPlacedBody(user, order);
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Async("emailExecutor")
    public void sendOrderShippedEmail(User user, Order order) {
        String subject = "🚀 Order Shipped – #" + order.getId() + " | " + order.getProductName();
        String body = buildOrderShippedBody(user, order);
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Async("emailExecutor")
    public void sendOrderDeliveredEmail(User user, Order order) {
        String subject = "✅ Order Delivered – #" + order.getId() + " | " + order.getProductName();
        String body = buildOrderDeliveredBody(user, order);
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Async("emailExecutor")
    public void sendOrderCancelledEmail(User user, Order order) {
        String subject = "❌ Order Cancelled – #" + order.getId() + " | " + order.getProductName();
        String body = buildOrderCancelledBody(user, order);
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    // ---------------------------------------------------------------
    // Admin Inactivity Reminder Email
    // ---------------------------------------------------------------

    @Async("emailExecutor")
    public void sendAdminReminderEmail(User admin) {
        String subject = "⚠️ Action Required – Pending Order Updates";
        String body = buildAdminReminderBody(admin);
        sendHtmlEmail(admin.getEmail(), subject, body);
    }

    // ---------------------------------------------------------------
    // Core Send Helper
    // ---------------------------------------------------------------

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // FIX 1: false = single-part mode (correct for HTML-only, no attachments).
            // true (multipart/mixed) caused Gmail SMTP to silently drop the message body.
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail, "Order Monitor Platform");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent → {} | {}", to, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Email Templates
    // ---------------------------------------------------------------

    private String header(String emoji, String color, String title) {
        return """
            <html>
            <body style="margin:0;padding:0;background:#0f172a;font-family:'Segoe UI',Arial,sans-serif;">
            <div style="max-width:600px;margin:32px auto;background:#1e293b;border-radius:16px;overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,0.4);">
              <div style="background:linear-gradient(135deg,%s,#312e81);padding:32px 40px;text-align:center;">
                <div style="font-size:48px;margin-bottom:8px;">%s</div>
                <h1 style="color:#fff;margin:0;font-size:24px;font-weight:700;">%s</h1>
                <p style="color:rgba(255,255,255,0.7);margin:8px 0 0;font-size:14px;">Order Monitor Platform</p>
              </div>
            """.formatted(color, emoji, title);
    }

    private String orderDetailTable(Order order) {
        return """
              <div style="padding:24px 40px;">
                <table style="width:100%%;border-collapse:collapse;border-radius:8px;overflow:hidden;">
                  <tr style="background:#334155;">
                    <td style="padding:12px 16px;color:#94a3b8;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;">Field</td>
                    <td style="padding:12px 16px;color:#94a3b8;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;">Details</td>
                  </tr>
                  <tr style="background:#1e293b;border-bottom:1px solid #334155;">
                    <td style="padding:12px 16px;color:#64748b;">Order ID</td>
                    <td style="padding:12px 16px;color:#e2e8f0;font-weight:600;">#%d</td>
                  </tr>
                  <tr style="background:#243246;border-bottom:1px solid #334155;">
                    <td style="padding:12px 16px;color:#64748b;">Product</td>
                    <td style="padding:12px 16px;color:#38bdf8;font-weight:700;font-size:16px;">%s</td>
                  </tr>
                  <tr style="background:#1e293b;border-bottom:1px solid #334155;">
                    <td style="padding:12px 16px;color:#64748b;">Category</td>
                    <td style="padding:12px 16px;">
                      <span style="background:#1e3a5f;color:#93c5fd;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:700;">%s</span>
                    </td>
                  </tr>
                  <tr style="background:#243246;border-bottom:1px solid #334155;">
                    <td style="padding:12px 16px;color:#64748b;">Quantity</td>
                    <td style="padding:12px 16px;color:#e2e8f0;font-weight:600;">%d units</td>
                  </tr>
                  <tr style="background:#1e293b;">
                    <td style="padding:12px 16px;color:#64748b;">Unit Price</td>
                    <td style="padding:12px 16px;color:#4ade80;font-weight:700;font-size:16px;">$%.2f</td>
                  </tr>
                </table>
              </div>
            """.formatted(
                order.getId(),
                order.getProductName(),
                order.getCategory(),
                order.getQuantity(),
                order.getPrice().doubleValue()
        );
    }

    private String footer() {
        return """
              <div style="padding:24px 40px;border-top:1px solid #334155;text-align:center;">
                <p style="color:#475569;font-size:12px;margin:0;">
                  © 2025 Order Monitor Platform &nbsp;|&nbsp;
                  <span style="color:#6366f1;">online.monitor.apt@gmail.com</span>
                </p>
              </div>
            </div>
            </body>
            </html>
            """;
    }

    private String buildOrderPlacedBody(User user, Order order) {
        return header("📋", "#4f46e5", "Order Placed Successfully!") +
            """
              <div style="padding:0 40px 8px;">
                <p style="color:#cbd5e1;font-size:16px;">Hi <strong style="color:#e2e8f0;">%s</strong>,</p>
                <p style="color:#94a3b8;font-size:14px;line-height:1.6;">
                  Your order has been placed successfully. Our team will process it shortly.
                </p>
              </div>
            """.formatted(user.getFullName()) +
            orderDetailTable(order) +
            """
              <div style="padding:0 40px 24px;">
                <div style="background:#1e3a5f;border-left:4px solid #3b82f6;padding:16px;border-radius:0 8px 8px 0;">
                  <p style="color:#93c5fd;margin:0;font-size:14px;">
                    ⏰ <strong>Next step:</strong> Complete your payment to confirm the order.
                    Orders must be shipped within <strong>24 hours</strong> of placement.
                  </p>
                </div>
              </div>
            """ +
            footer();
    }

    private String buildOrderShippedBody(User user, Order order) {
        return header("🚀", "#2563eb", "Your Order is On Its Way!") +
            """
              <div style="padding:0 40px 8px;">
                <p style="color:#cbd5e1;font-size:16px;">Hi <strong style="color:#e2e8f0;">%s</strong>,</p>
                <p style="color:#94a3b8;font-size:14px;line-height:1.6;">
                  Great news! Your order has been shipped and is on its way to you.
                </p>
              </div>
            """.formatted(user.getFullName()) +
            orderDetailTable(order) +
            """
              <div style="padding:0 40px 24px;">
                <div style="background:#1e3a5f;border-left:4px solid #06b6d4;padding:16px;border-radius:0 8px 8px 0;">
                  <p style="color:#67e8f9;margin:0;font-size:14px;">
                    📦 Expected delivery within <strong>48 hours</strong> of shipment.
                  </p>
                </div>
              </div>
            """ +
            footer();
    }

    private String buildOrderDeliveredBody(User user, Order order) {
        return header("✅", "#059669", "Order Delivered – Complete!") +
            """
              <div style="padding:0 40px 8px;">
                <p style="color:#cbd5e1;font-size:16px;">Hi <strong style="color:#e2e8f0;">%s</strong>,</p>
                <p style="color:#94a3b8;font-size:14px;line-height:1.6;">
                  Your order has been successfully delivered!
                </p>
              </div>
            """.formatted(user.getFullName()) +
            orderDetailTable(order) +
            """
              <div style="padding:0 40px 24px;">
                <div style="background:#064e3b;border-left:4px solid #10b981;padding:16px;border-radius:0 8px 8px 0;">
                  <p style="color:#6ee7b7;margin:0;font-size:14px;">
                    🎉 Order complete. Thank you for shopping with us!
                  </p>
                </div>
              </div>
            """ +
            footer();
    }

    private String buildOrderCancelledBody(User user, Order order) {
        return header("❌", "#dc2626", "Order Cancelled") +
            """
              <div style="padding:0 40px 8px;">
                <p style="color:#cbd5e1;font-size:16px;">Hi <strong style="color:#e2e8f0;">%s</strong>,</p>
                <p style="color:#94a3b8;font-size:14px;line-height:1.6;">
                  Your order has been cancelled as requested.
                </p>
              </div>
            """.formatted(user.getFullName()) +
            orderDetailTable(order) +
            """
              <div style="padding:0 40px 24px;">
                <div style="background:#450a0a;border-left:4px solid #ef4444;padding:16px;border-radius:0 8px 8px 0;">
                  <p style="color:#fca5a5;margin:0;font-size:14px;">
                    If you did not request this cancellation, please contact support immediately.
                  </p>
                </div>
              </div>
            """ +
            footer();
    }

    private String buildAdminReminderBody(User admin) {
        return header("⚠️", "#d97706", "Pending Order Updates Required") +
            """
              <div style="padding:0 40px 8px;">
                <p style="color:#cbd5e1;font-size:16px;">Hi <strong style="color:#e2e8f0;">%s</strong>,</p>
                <p style="color:#94a3b8;font-size:14px;line-height:1.6;">
                  You have been inactive on the Order Monitor dashboard for over <strong style="color:#fbbf24;">8 hours</strong>.
                </p>
              </div>
              <div style="padding:0 40px 24px;">
                <div style="background:#451a03;border-left:4px solid #f59e0b;padding:20px;border-radius:0 8px 8px 0;">
                  <p style="color:#fde68a;margin:0 0 12px;font-size:15px;font-weight:700;">Action Required</p>
                  <ul style="color:#fcd34d;margin:0;padding-left:20px;font-size:14px;line-height:2;">
                    <li>Please check pending order status updates in your dashboard.</li>
                    <li>Orders must be shipped within 24 hours of placement.</li>
                    <li>Shipped orders must be delivered within 48 hours.</li>
                  </ul>
                </div>
                <div style="margin-top:24px;text-align:center;">
                  <a href="#" style="background:linear-gradient(135deg,#f59e0b,#d97706);color:#fff;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:700;font-size:15px;display:inline-block;">
                    🖥️ Open Dashboard
                  </a>
                </div>
              </div>
            """.formatted(admin.getFullName()) +
            footer();
    }
}