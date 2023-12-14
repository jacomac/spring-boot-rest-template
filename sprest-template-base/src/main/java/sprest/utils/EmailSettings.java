package sprest.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration used to construct e-mail messages.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "sprest.emails")
public class EmailSettings {

    private String fromEmail;
    private String fromName;
    private String passwordResetSubject;
}
