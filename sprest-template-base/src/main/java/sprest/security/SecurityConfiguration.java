package sprest.security;

import sprest.user.UserService;
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

import static sprest.user.BaseRight.values.MANAGE_SYSTEM_SETTINGS;

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
    private UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository cookieCsrfTokenRepository =
            CookieCsrfTokenRepository.withHttpOnlyFalse();
        cookieCsrfTokenRepository.setCookiePath("/");

        var securityConfiguration = http
            .securityContext((securityContext) -> securityContext.requireExplicitSave(true))
            .authorizeHttpRequests()
            .requestMatchers("/index.html", "/", "/logout").permitAll()
            .requestMatchers("/users/password-resets/**").permitAll()
            .requestMatchers("/announcement").permitAll()
            .requestMatchers("/actuator/**").hasAuthority(MANAGE_SYSTEM_SETTINGS)
            .anyRequest().authenticated()
            .and()
            .httpBasic()
            .and()
            .cors()
            .and()
            .csrf()
            .csrfTokenRepository(cookieCsrfTokenRepository)
            .ignoringRequestMatchers("/ext/**"); // namespace for services exposed to the outside (INVOKE rights intended to be used here)

        return securityConfiguration.and().build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new AuthorizationInterceptor(appContext, userService));
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
