package com.cpt202.projectselection.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Primary
public class SmtpActivationMailService implements ActivationMailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpActivationMailService.class);

    private final JavaMailSender mailSender;

    public SmtpActivationMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendActivation(String email, String activationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("unistatus@163.com"); // 必须与 SMTP 授权用户一致
        message.setTo(email);
        message.setSubject("CPT202 Project Selection - Account Activation");
        message.setText("Please click the following link to activate your account:\n\n"
                + activationLink
                + "\n\nThis link will expire in 3 hours.\n"
                + "If you did not register for this system, please ignore this email.");

        try {
            mailSender.send(message);
            log.info("Activation email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send activation email to {}", email, e);
        }
    }
}
