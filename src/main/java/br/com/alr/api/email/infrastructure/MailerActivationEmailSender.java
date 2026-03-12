package br.com.alr.api.email.infrastructure;

import br.com.alr.api.email.domain.ActivationEmailSender;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MailerActivationEmailSender implements ActivationEmailSender {

  private static final Logger LOG = Logger.getLogger(MailerActivationEmailSender.class);

  @Inject
  Mailer mailer;

  @ConfigProperty(name = "app.mail.from")
  String from;

  @ConfigProperty(name = "app.activation.base-url")
  String activationBaseUrl;

  @Override
  public void sendActivationEmail(String username, String firstName, Long userId) {
    String activationLink = activationBaseUrl + "/users/" + userId + "/activate";
    String body = "Hello " + firstName + ", activate your account: " + activationLink;

    Mail mail = Mail.withText(username, "Activate your account", body)
        .setFrom(from);
    mailer.send(mail);
    LOG.infov("Activation email sent. userId={0}, username={1}, activationLink={2}", userId, username, activationLink);
  }
}
