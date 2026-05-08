package com.cpt202.projectselection.service;

public interface ActivationMailService {

    void sendActivation(String email, String activationLink);
}
