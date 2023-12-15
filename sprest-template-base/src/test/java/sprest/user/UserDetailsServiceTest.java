package sprest.user;

import sprest.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTest {

    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserDetailsService userDetailsService;

    @Test
    public void mustLoadUserByClientShortcutAndUserName() {
        // given
        var clientShortcut = "cli";
        var userName = "user";
        var user = new AppUser();
        user.setUserName(userName);

        // when
        Mockito.when(userRepository.findByUserName(userName))
            .thenReturn(Optional.of(user));
        var foundUser = userDetailsService.loadUserByUsername(
            String.join("/", clientShortcut, userName));

        // then
        assertEquals(userName, foundUser.getUsername());
    }

    @Test
    public void mustLoadUserByEmail() {
        // given
        var email = "user@email.xyz";
        var user = new AppUser();
        user.setUserName(email);

        // when
        Mockito.when(userRepository.findByEmailIgnoreCase(email))
            .thenReturn(Optional.of(user));
        var foundUser = userDetailsService.loadUserByUsername(email);

        // then
        assertEquals(email, foundUser.getUsername());
    }

    @Test
    public void mustThrowExceptionWhenUserNotFound() {
        // given
        var clientShortcut = "cli";
        var userName = "user";

        // when
        Mockito.when(userRepository.findByUserName(userName))
            .thenReturn(Optional.empty());
        UsernameNotFoundException e = assertThrows(UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(String.join("/", clientShortcut, userName)));

        // then
        assertTrue(e.getMessage().contains(String.join("/", clientShortcut, userName)));
    }
}
