package sprest.security;

import lombok.NoArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.stereotype.Component;

/**
 * Performs security checks.
 *
 */
@Component("securityManager")
@NoArgsConstructor
public class SecurityManager {

    public static CorsConfigurer<HttpSecurity> configureCommon(HttpSecurity http) throws Exception {
        return http
            .securityContext((securityContext) -> securityContext.requireExplicitSave(true))
            .authorizeHttpRequests()
            .antMatchers("/index.html", "/", "/logout").permitAll()
            .antMatchers("/users/password-resets/**").permitAll()
            .antMatchers("/announcement").permitAll()
            .anyRequest().authenticated()
            .and()
            .httpBasic()
            .and()
            .cors();

    }
}
