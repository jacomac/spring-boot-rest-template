package sprest.security;

import sprest.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The security configuration used in production with strict security settings
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
    @Value("${sprest.baseUrl}")
    private String baseUrl;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private UserManager userManager;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository cookieCsrfTokenRepository =
            CookieCsrfTokenRepository.withHttpOnlyFalse();
        cookieCsrfTokenRepository.setCookiePath("/");

        var securityConfiguration = SecurityManager.configureCommon(http)
            .and()
            .csrf()
            .csrfTokenRepository(cookieCsrfTokenRepository)
            .ignoringRequestMatchers("/ext/**");

        return securityConfiguration.and().build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new AuthorizationInterceptor(appContext, userManager));
            }

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/ext/**")
                    .allowedMethods("*")
                    .allowedOriginPatterns("*");

                registry.addMapping("/**")
                    .allowedMethods("*")
                    .allowedOriginPatterns(baseUrl)
                    .allowCredentials(true);
            }
        };
    }

}
