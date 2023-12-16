package sprest.admin;

import sprest.api.RequiredAccessRight;
import sprest.exception.DuplicateUserException;
import sprest.user.*;
import sprest.user.dtos.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import sprest.user.dtos.UserDtoWithId;

import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static sprest.user.UserRight.values.MANAGE_USERS;

/**
 * Controller for administering all the user master data in the system
 * @author wulf
 */
@Tag(name = "User Admin Controller", description = "API to manage users, requires the MANAGE_USERS privilege")
@RestController
@RequestMapping("/admin/users")
@RequiredAccessRight(MANAGE_USERS)
public class UserAdminController {
    private static final String MSG_USER_NOT_FOUND = "Benutzer wurde nicht gefunden";
    private static final String IMPERSONATION_ADMIN_ATTRIBUTE = "SPREST_IMPERSONATION_ADMIN";

    @Autowired
    private UserManager userManager;

	// read single
	@GetMapping("/{id}")
	@RequiredAccessRight(MANAGE_USERS)
	public UserDtoWithId getUser(@PathVariable("id") int id) {
		var user = userManager.getById(id);
        user.setPassword(null);

        return user.toUserDtoWithId();
	}

	@Operation(summary = "Get a list of all users in the system that is pageable, sortable and can be filtered",
			description = "Returns the list of users for the current client. If the caller also has the MANAGE_ANNOUNCEMENTS privilege, the list will span all clients."
					+ " The list is pageable, sortable and can be filtered by multiple criteria (AND-connected with string comparison operator 'contains'). "
					+ " To apply the specified filter, you need to set 'useFilter' to true")
	@PageableAsQueryParam
	@GetMapping
	@RequiredAccessRight({MANAGE_USERS})
    public Page<UserAdminDto> getUsersInPages(Principal auth, Optional<UserSearchFilter> filter, Pageable pageable) {
        if (auth == null)
            return null;
        AppUser principal = userManager.getUserByPrincipal(auth);
        assert principal != null;
        return (filter.isPresent() && filter.get().isUseFilter())
            ? userPageToAdminDto(userManager.getFilteredUsers(filter.get(), pageable))
            : userPageToAdminDto(userManager.getUsers(pageable));
    }

	private Page<UserAdminDto> userPageToAdminDto(Page<AppUser> users) {
		return users.map(AppUser::toAdminDto);
	}

	@Operation(summary = "create a new user in the system",
			description = "creates a new user for the client of the calling user. If the user has the MANAGE_ANNOUNCEMENTS privilege and an id of an existing client is provided, the user will be created for the desired client instead.")
	@PostMapping
	public AppUser saveNewUser(@Valid @RequestBody UserDto user, Principal auth) {
		try {
			return userManager.createUser(user, userManager.getUserByPrincipal(auth));
		} catch (DuplicateUserException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
	}

	@PutMapping("/{id}")
	public UserDtoWithId updateUser(@PathVariable("id") int id, @Valid @RequestBody UserDto user, Principal auth) {
		try {
			return userManager.updateUser(id, user, userManager.getUserByPrincipal(auth));
		} catch (NoSuchElementException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_USER_NOT_FOUND, e);
		} catch (DuplicateUserException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		}
	}

	@PutMapping("/password-resets/{userId}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void resetPasswordForUser(@PathVariable("userId") int userId) {
        userManager.sendResetPasswordLink(userId);
	}

	@Operation(summary = "get the rights that can be assigned by the given user manager",
			description = "returns a list of the UserRights that can be assigned to another user administered by the given principal.")
	@GetMapping("/rights/grantable")
	@ResponseBody
	public Iterable<AccessRight> getGrantableRights(Principal auth) {
		AppUser principal = userManager.getUserByPrincipal(auth);
		return userManager.getGrantableRights(principal);
	}

	// deactivate
	@PutMapping("/deactivate/{userId}")
	public UserDtoWithId deactivateUser(@PathVariable("userId") int id) {
		return toggleActive(id, false);
	}

	// activate
	@PutMapping("/activate/{userId}")
	public UserDtoWithId activateUser(@PathVariable("userId") int id) {
		return toggleActive(id, true);
	}

	private UserDtoWithId toggleActive(int id, boolean isActive) {
        return userManager.toggleUserIsActive(id, isActive).toUserDtoWithId();
	}

	@GetMapping("/username-available/{clientId}/{username}")
	public boolean checkIfUsernameIsAvailable(@PathVariable("clientId") Integer clientId, @PathVariable("username") String username) {
		return !userManager.existsByClientIdAndUserName(clientId, username);
	}

	@GetMapping("/email-available/{email}")
	public boolean checkIfEmailIsAvailable(@PathVariable("email") String email) {
		return !userManager.existsByEmail(email);
	}
}
