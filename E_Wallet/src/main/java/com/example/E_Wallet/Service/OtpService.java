package com.example.E_Wallet.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.example.E_Wallet.Model.Otp;
import com.example.E_Wallet.Model.Transaction;
import com.example.E_Wallet.Repository.OtpRepo;
import com.example.E_Wallet.Repository.TransactionRepo;
import com.example.E_Wallet.Exceptions.ResourceNotFoundException;
import com.example.E_Wallet.Exceptions.ValidationException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class OtpService {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private OtpRepo otpRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3;

    public String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public Otp createAndSendOtp(UUID transactionId, UUID userId, String userEmail, String transactionType) {
        
        Optional<Otp> existingOtp = otpRepo.findByTransactionId(transactionId);
        if (existingOtp.isPresent() && !existingOtp.get().getIsVerified()) {
            
            Otp otp = existingOtp.get();
            String newOtpCode = generateOtp();

            String hashedOtp = passwordEncoder.encode(newOtpCode);
            otp.setOtpCode(hashedOtp);
            otp.setCreatedAt(LocalDateTime.now());
            otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
            otp.setAttemptCount(0);
            otp.setIsExpired(false);
            otp = otpRepo.save(otp);
            // Send plain OTP in email (not hashed)
            sendOtpEmail(userEmail, newOtpCode, transactionType);
            return otp;
        }

        // Create new OTP
        String otpCode = generateOtp();
        LocalDateTime now = LocalDateTime.now();
        
        
        String hashedOtp = passwordEncoder.encode(otpCode);
        
        Otp otp = new Otp();
        otp.setTransactionId(transactionId);
        otp.setUserId(userId);
        otp.setOtpCode(hashedOtp); // Store hashed OTP
        otp.setUserEmail(userEmail);
        otp.setTransactionType(transactionType);
        otp.setCreatedAt(now);
        otp.setExpiresAt(now.plusMinutes(OTP_EXPIRY_MINUTES));
        otp.setAttemptCount(0);
        otp.setIsVerified(false);
        otp.setIsExpired(false);

        otp = otpRepo.save(otp);
        // Send plain OTP in email 
        sendOtpEmail(userEmail, otpCode, transactionType);
        return otp;
    }

    
    public boolean verifyOtp(UUID transactionId, String enteredOtp) {
        
        Otp otp = otpRepo.findByTransactionIdAndIsVerifiedFalse(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found or already verified for transaction: " + transactionId));

        // Check if OTP has expired
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            markOtpAsExpired(transactionId);
            markTransactionAsFailed(transactionId, "OTP expired");
            throw new ValidationException("OTP has expired. Please initiate a new transaction.");
        }

       
        if (otp.getAttemptCount() >= MAX_ATTEMPTS) {
            markTransactionAsFailed(transactionId, "Wrong OTP");
            throw new ValidationException("Transaction has failed. Maximum OTP verification attempts exceeded.");
        }

        
        // Verify entered OTP against hashed OTP in database
        if (passwordEncoder.matches(enteredOtp, otp.getOtpCode())) {
            markOtpAsVerified(transactionId);
            return true;
        } else {
            
            int newAttemptCount = incrementAttemptCount(transactionId);
            
            if (newAttemptCount >= MAX_ATTEMPTS) {
                markTransactionAsFailed(transactionId, "Wrong OTP");
                throw new ValidationException("Transaction has failed. Maximum OTP verification attempts exceeded.");
            }
            
            int remainingAttempts = MAX_ATTEMPTS - newAttemptCount;
            throw new ValidationException("Incorrect OTP. Attempts remaining: " + remainingAttempts);
        }
    }
    
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int incrementAttemptCount(UUID transactionId) {
        Otp otp = otpRepo.findByTransactionIdAndIsVerifiedFalse(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found"));
        int newCount = otp.getAttemptCount() + 1;
        otp.setAttemptCount(newCount);
        otpRepo.saveAndFlush(otp);
        return newCount;
    }
    
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOtpAsVerified(UUID transactionId) {
        Otp otp = otpRepo.findByTransactionIdAndIsVerifiedFalse(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found"));
        otp.setIsVerified(true);
        otpRepo.saveAndFlush(otp);
    }
    
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOtpAsExpired(UUID transactionId) {
        Otp otp = otpRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found"));
        otp.setIsExpired(true);
        otpRepo.saveAndFlush(otp);
    }

    
    private void markTransactionAsFailed(UUID transactionId, String remark) {
        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
        
        transaction.setStatus("failed");
        transaction.setRemarks(remark);
        transactionRepo.save(transaction);
    }

    
    private void sendOtpEmail(String toEmail, String otpCode, String transactionType) {
        try {
            String subject = "E-Wallet Transaction OTP - " + transactionType;
            String emailBody = buildOtpEmailBody(otpCode, transactionType);
            emailService.sendSimpleEmail(toEmail, subject, emailBody);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }

   
    private String buildOtpEmailBody(String otpCode, String transactionType) {
        return "E-Wallet Transaction OTP\n\n" +
               "Dear User,\n\n" +
               "You have initiated a " + transactionType + " transaction on your E-Wallet account.\n\n" +
               "Please use the following OTP to complete your transaction:\n\n" +
               otpCode + "\n\n" +
               "Important:\n" +
               "- This OTP is valid for 10 minutes only\n" +
               "- Do not share this OTP with anyone\n" +
               "- You have 3 attempts to enter the correct OTP\n\n" +
               "If you did not initiate this transaction, please ignore this email or contact support immediately.\n\n" +
               "This is an automated email. Please do not reply.";
    }

    
    public int getRemainingAttempts(UUID transactionId) {
        Optional<Otp> otpOpt = otpRepo.findByTransactionId(transactionId);
        if (otpOpt.isPresent()) {
            Otp otp = otpOpt.get();
            return Math.max(0, MAX_ATTEMPTS - otp.getAttemptCount());
        }
        return 0;
    }
}


