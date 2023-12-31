package sprest.user;

import sprest.exception.DuplicateUserException;
import sprest.exception.InavlidOrExpiredPasswordResetTokenException;
import sprest.user.dtos.PasswordResetRequest;
import sprest.user.dtos.PasswordResetResponse;
import sprest.user.dtos.UserDtoWithId;
import sprest.user.dtos.UserSelfAdminDto;
import sprest.user.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.security.Principal;

/**
 * Controller for all operations of a user upon his own account
 *
 * @author wulf
 */
@Tag(name = "User Controller", description = "API for accessing user information, usable by all users")
@RestController
@Transactional
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/current")
    public AppUser user(Principal auth) {
        return userService.getUserByPrincipal(auth);
    }

    @Operation(summary = "update own user information", description = "method for updating the information on one's own user account (self admin)")
    @PutMapping("/update-current")
    public UserDtoWithId changeUser(@Valid @RequestBody UserSelfAdminDto user, Principal auth) {
        try {
            AppUser principal = userService.getUserByPrincipal(auth);
            return userService.changeSelfUserData(user, principal);
        } catch (DuplicateUserException | AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/logout/current")
    public String logout(HttpSession session, Principal user) {
        if (session != null) {
            session.invalidate();
        }
        return "User " + user.getName() + " has been logged out";
    }

    @Operation(
        summary = "Send password reset request",
        description = "Send password reset request to given email if found in user database.",
        responses = @ApiResponse(responseCode = "202", description = "Returns ACCEPTED no mater if email exists or not."))
    @PostMapping("/password-resets")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendResetPasswordLink(@RequestBody @Valid PasswordResetRequest request) {
        userService.sendResetPasswordLink(request);
    }

    @Operation(
        summary = "Allows the user to get new generated password.",
        description = "Sets new random password for a user if provided token is valid and returns it in the response.",
        responses = {
            @ApiResponse(responseCode = "200", description = "When password changed successfully."),
            @ApiResponse(responseCode = "400", description = "When token is invalid or expired.")
        })
    @GetMapping("/password-resets/{token}")
    @ResponseStatus(HttpStatus.OK)
    public PasswordResetResponse resetPassword(@PathVariable("token") String token)
        throws InavlidOrExpiredPasswordResetTokenException {
        var password = userService.resetPassword(token);

        return new PasswordResetResponse(password);
    }

    @PutMapping("/resetPassword")
    public String resetPassword(Principal auth) {
        var user = userService.getUserByPrincipal(auth);
        return userService.resetPassword(user.getId());
    }

    @GetMapping("/rights")
    public Iterable<String> getAvailableUserRights() {
        return userService.getAvailableRights();
    }

}
