package sprest.utils;

import sprest.exception.EmailException;
import sprest.user.AppUser;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generates email content from Freemarker templates.
 */
@Component
public class EmailContentProvider {

    private static final String PASSWORD_RESET_TEMPLATE = "password-reset.ftl";
    private static final String NEW_PRESCRIPTION_TEMPLATE = "new-prescription-notification.ftl";

    private final Configuration freemarker;
    private final URL baseUrl;

    public EmailContentProvider(@Value("${sprest.baseUrl}") URL baseUrl, Configuration freemarker) {
        Objects.requireNonNull(freemarker);
        Objects.requireNonNull(baseUrl);
        this.freemarker = freemarker;
        this.baseUrl = baseUrl;
    }

    public String forResetPassword(AppUser user) {
        StringWriter emailContent = new StringWriter();
        var params = new HashMap<String, String>(4);
        params.put("baseUrl", baseUrl.toString());
        params.put("token", user.getPasswordResetToken());
        params.put("loginName", user.getUserName());
        params.put("email", user.getEmail());
        params.put("name", user.getFirstName() + " " + user.getLastName());
        try {
            freemarker.getTemplate(PASSWORD_RESET_TEMPLATE).process(params, emailContent);
        } catch (TemplateException | IOException e) {
            throw new EmailException("Failed to generate email content from template.", e);
        }

        return emailContent.toString();
    }

    public String forNewPrescription(int prescriptionContainerId) {
        StringWriter emailContent = new StringWriter();
        var params = new HashMap<String, String>(2);
        params.put("baseUrl", baseUrl.toString());
        params.put("prescriptionContainerId", String.valueOf(prescriptionContainerId));
        try {
            freemarker.getTemplate(NEW_PRESCRIPTION_TEMPLATE).process(params, emailContent);
        } catch (TemplateException | IOException e) {
            throw new EmailException("Failed to generate email content from template.", e);
        }

        return emailContent.toString();
    }
}
