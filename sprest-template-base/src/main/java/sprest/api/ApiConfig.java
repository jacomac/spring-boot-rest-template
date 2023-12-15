package sprest.api;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * this structures the OpenAPi for the rest services as well as it illustrates
 * how the URL path structures shall be.
 *
 * @author wulf
 *
 */
@Configuration
public class ApiConfig {

    private final String externalApiPaths[] = { "/ext/**" };

    /**
     * this tags all REST endpoints that are used to manage the ATW master data
     */
    @Bean
    public GroupedOpenApi adminApi() {
        String paths[] = { "/admin/**" };
        return GroupedOpenApi.builder().group("admin").pathsToMatch(paths).build();
    }

    /**
     * this tags all REST endpoints that are used by the support team to debug
     * issues and monitor system health
     */
    @Bean
    public GroupedOpenApi supportApi() {
        String paths[] = { "/actuator/**" };
        return GroupedOpenApi.builder().group("supportability").pathsToMatch(paths).build();
    }

    /**
     * this tags all REST endpoints available internally
     */
    @Bean
    public GroupedOpenApi allInternalApi() {
        return GroupedOpenApi.builder().group("internal-api-all").pathsToExclude(externalApiPaths).build();
    }

}
