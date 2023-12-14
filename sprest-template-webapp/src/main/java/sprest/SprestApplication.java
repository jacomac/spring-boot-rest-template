package sprest;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import sprest.utils.EmailSettings;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({EmailSettings.class})
public class SprestApplication {

    public static void main(String[] args) {
        log.info("STARTING Sprest Backend");
        ConfigurableApplicationContext ctx = SpringApplication.run(SprestApplication.class, args);
        SprestApplication app = ctx.getBean(SprestApplication.class);
        log.info("Sprest Backend STARTED");
    }

    // see https://stackoverflow.com/questions/46251131/invalid-character-found-in-the-request-target-in-spring-boot
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]");
            }
        });
        return factory;
    }

    @Autowired
    private Environment environment;

}
