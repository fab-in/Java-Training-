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

    public void sendStatementEmail(String toEmail, String userName, byte[] csvBytes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, 
                true, 
                StandardCharsets.UTF_8.name()
        );

        
        helper.setTo(toEmail);
        helper.setSubject("Your E-Wallet Transaction Statement");
        
        String emailBody = buildEmailBody(userName);
        helper.setText(emailBody, true); // true = HTML format

        String fileName = "wallet-statement-" + java.time.LocalDate.now() + ".csv";
        helper.addAttachment(fileName, new ByteArrayResource(csvBytes), "text/csv");

        mailSender.send(message);
    }

    public void sendSimpleEmail(String toEmail, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, 
                false, 
                StandardCharsets.UTF_8.name()
        );

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, false); 

        mailSender.send(message);
    }

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
               "</ul>"  +
               "</body>" +
               "</html>";
    }
}

