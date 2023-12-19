package sprest.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import sprest.user.AppUser;
import sprest.user.UserPrincipal;

public class AuthenticationHolder {
    public static AppUser getCurrentUser() {
        return getCurrentUserPrincipal().getUser();
    }

    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        var principal = auth.getPrincipal();
        if (principal == null)
            throw new RuntimeException("unauthorized");
        if (!(principal instanceof UserPrincipal))
            throw new RuntimeException("wrong principal: " + principal.getClass());
        return (UserPrincipal) principal;
    }
}
