package br.com.alr.api.email.domain;

public interface ActivationEmailSender {

  void sendActivationEmail(String username, String firstName, Long userId);
}
