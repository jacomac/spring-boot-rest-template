package sprest.user;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private AppUser user;

    public UserPrincipal(AppUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new LinkedList<>();
        for (AccessRight accessRight : user.getAccessRights()) {
            GrantedAuthority authority = new SimpleGrantedAuthority(accessRight.getName());
            authorities.add(authority);
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    public AppUser getUser() {
        return user;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("Username=")
            .append(user.getUserName())
            .append(",Id=")
            .append(user.getId());

        return sb.toString();
    }

    private static final long serialVersionUID = 8949925912494283282L;

}
