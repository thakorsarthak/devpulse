package com.devpulse.notification.service;

import com.devpulse.notification.dto.AiIncidentAnalyzedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final AuthServiceClient authServiceClient;

    @Value("${notification.email.from:shopcapital9@gmail.com}")
    private String fromEmail;

    public void sendIncidentAlert(AiIncidentAnalyzedMessage message) {
        // Resolve recipient dynamically from auth-service
        String recipientEmail = authServiceClient
                .getServiceOwnerEmail(message.getServiceName());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail); // dynamic now
            helper.setSubject(buildSubject(message));
            helper.setText(buildHtmlBody(message), true);

            mailSender.send(mimeMessage);
            log.info("Sent incident alert to {} for service: {}",
                    recipientEmail, message.getServiceName());

        } catch (Exception e) {
            log.error("Failed to send email to {} for service: {}",
                    recipientEmail, message.getServiceName(),  e);
        }
    }



    private String buildSubject(AiIncidentAnalyzedMessage message) {
        String status = message.isAnalysisSuccessful()
                ? "🔴 AI Incident Alert"
                : "🔴 Incident Alert";
        return String.format("%s — %s", status,
                message.getServiceName());
    }

    private String buildHtmlBody(AiIncidentAnalyzedMessage message) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;
                             max-width: 600px; margin: 0 auto;">

                    <div style="background: #dc3545; color: white;
                                padding: 20px; border-radius: 8px 8px 0 0;">
                        <h2 style="margin: 0;">🚨 Production Incident Detected</h2>
                        <p style="margin: 5px 0 0 0;">Service: <strong>%s</strong></p>
                    </div>

                    <div style="background: #f8f9fa; padding: 20px;
                                border: 1px solid #dee2e6;">

                        <h3 style="color: #dc3545;">Error</h3>
                        <pre style="background: #fff; padding: 10px;
                                   border-left: 4px solid #dc3545;
                                   overflow-x: auto;">%s</pre>

                        <h3 style="color: #0d6efd;">AI Explanation</h3>
                        <p style="background: #fff; padding: 10px;
                                  border-left: 4px solid #0d6efd;">%s</p>

                        <h3 style="color: #198754;">Suggested Fix</h3>
                        <p style="background: #fff; padding: 10px;
                                  border-left: 4px solid #198754;">%s</p>

                        <hr style="border: 1px solid #dee2e6; margin: 20px 0;">
                        <p style="color: #6c757d; font-size: 12px;">
                            Error Log ID: %d |
                            Analyzed at: %s |
                            AI Analysis: %s
                        </p>
                    </div>

                </body>
                </html>
                """,
                message.getServiceName(),
                message.getOriginalError(),
                message.getAiExplanation() != null
                        ? message.getAiExplanation()
                        : "No explanation available",
                message.getSuggestedFix() != null
                        ? message.getSuggestedFix()
                        : "No fix suggested",
                message.getErrorLogId(),
                message.getAnalyzedAt(),
                message.isAnalysisSuccessful() ? "✅ Success" : "⚠️ Unavailable"
        );
    }
}