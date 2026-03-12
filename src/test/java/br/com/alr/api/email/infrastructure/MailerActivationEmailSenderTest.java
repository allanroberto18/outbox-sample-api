package br.com.alr.api.email.infrastructure;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MailerActivationEmailSenderTest {

    @Test
    void shouldSendActivationEmailWithExpectedContent() {
        Mailer mailer = mock(Mailer.class);

        MailerActivationEmailSender sender = new MailerActivationEmailSender();
        sender.mailer = mailer;
        sender.from = "no-reply@test.local";
        sender.activationBaseUrl = "http://localhost:8080";

        sender.sendActivationEmail("john@mail.test", "John", 42L);

        ArgumentCaptor<Mail> captor = ArgumentCaptor.forClass(Mail.class);
        verify(mailer).send(captor.capture());

        Mail mail = captor.getValue();
        assertEquals("no-reply@test.local", mail.getFrom());
        assertEquals("Activate your account", mail.getSubject());
        assertTrue(mail.getTo().contains("john@mail.test"));
        assertTrue(mail.getText().contains("Hello John"));
        assertTrue(mail.getText().contains("http://localhost:8080/users/42/activate"));
    }
}
