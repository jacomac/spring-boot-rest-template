package sprest.user;

import sprest.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component("userDetailsService")
@Slf4j
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		var user = username.contains("@")?
            getUserByEmail(username) : getUserByUserName(username);

		return new UserPrincipal(user);
	}

    private User getUserByUserName(String username) {
        log.debug("authenticate: user {}", username);

        return userRepository.findByUserName(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    private User getUserByEmail(String username) {
        log.debug("authenticate: email {}", username);

        return userRepository.findByEmailIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
