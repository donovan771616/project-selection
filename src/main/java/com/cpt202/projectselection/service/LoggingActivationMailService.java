package com.cpt202.projectselection.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LoggingActivationMailService implements ActivationMailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingActivationMailService.class);

    @Override
    @Async
    public void sendActivation(String email, String activationLink) {
        log.info("Activation email for {}: {}", email, activationLink);
    }
}
