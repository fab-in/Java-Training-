package com.example.E_Wallet.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a transaction statement CSV file as an email attachment
     * 
     * @param toEmail Recipient email address
     * @param userName Name of the user receiving the statement
     * @param csvBytes CSV file content as byte array
     * @throws MessagingException if email sending fails
     */
    public void sendStatementEmail(String toEmail, String userName, byte[] csvBytes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, 
                true, // multipart = true for attachments
                StandardCharsets.UTF_8.name()
        );

        // Set email details
        helper.setTo(toEmail);
        helper.setSubject("Your E-Wallet Transaction Statement");
        
        // Email body
        String emailBody = buildEmailBody(userName);
        helper.setText(emailBody, true); // true = HTML format

        // Attach CSV file
        String fileName = "wallet-statement-" + java.time.LocalDate.now() + ".csv";
        helper.addAttachment(fileName, new ByteArrayResource(csvBytes), "text/csv");

        // Send email
        mailSender.send(message);
    }

    /**
     * Builds the HTML email body
     */
    private String buildEmailBody(String userName) {
        return "<html>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<h2 style='color: #4CAF50;'>E-Wallet Transaction Statement</h2>" +
               "<p>Dear " + (userName != null ? userName : "User") + ",</p>" +
               "<p>Thank you for using our E-Wallet service. Please find your transaction statement attached to this email.</p>" +
               "<p>The statement includes all your transaction history with details such as:</p>" +
               "<ul>" +
               "<li>Transaction dates and times</li>" +
               "<li>Transaction types (Debit/Credit)</li>" +
               "<li>Amounts</li>" +
               "<li>Transaction status</li>" +
               "<li>Wallet account numbers</li>" +
               "</ul>" +
               "<p>If you have any questions or concerns about your transactions, please contact our support team.</p>" +
               "<p>Best regards,<br>E-Wallet Team</p>" +
               "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
               "<p style='font-size: 12px; color: #666;'>This is an automated email. Please do not reply to this message.</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
}

