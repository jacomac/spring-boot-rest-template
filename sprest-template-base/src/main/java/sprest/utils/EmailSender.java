package sprest.utils;

import sprest.exception.EmailException;
import sprest.user.AppUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class EmailSender {

    private final JavaMailSender mailSender;
    private final EmailSettings emailSettings;
    private final EmailContentProvider emailContentProvider;

    public EmailSender(JavaMailSender mailSender, EmailSettings emailSettings,
        EmailContentProvider emailContentProvider) {
        Objects.requireNonNull(mailSender);
        Objects.requireNonNull(emailSettings);
        Objects.requireNonNull(emailContentProvider);
        this.emailSettings = emailSettings;
        this.mailSender = mailSender;
        this.emailContentProvider = emailContentProvider;
    }

    public void sendPasswordResetEmail(AppUser user) throws EmailException {
        log.debug("Sending password reset email to {}.", user.getEmail());
        try {
            String messageContent = emailContentProvider.forResetPassword(user);
            MimeMessage message = createMimeMessage(Set.of(user.getEmail()),
                emailSettings.getPasswordResetSubject(), messageContent);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new EmailException("Failed to send email.", e);
        }
    }

    private MimeMessage createMimeMessage(Set<String> emails, String subject, String messageContent)
        throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,
            StandardCharsets.UTF_8.name());
        messageHelper.setSubject(subject);
        messageHelper.setBcc(emails.toArray(new String[]{}));
        messageHelper.setFrom(emailSettings.getFromEmail(), emailSettings.getFromName());
        messageHelper.setText(messageContent, true);

        return mimeMessage;
    }
}
