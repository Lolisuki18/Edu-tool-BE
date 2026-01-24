package com.edutool.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${PORT}")
    private String serverPort;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Email Verification");

        String verificationLink = "http://localhost:" + serverPort + "/auth/verify?token=" + token;
        message.setText("Please verify your email by clicking the link: " + verificationLink);

        mailSender.send(message);
    }
}