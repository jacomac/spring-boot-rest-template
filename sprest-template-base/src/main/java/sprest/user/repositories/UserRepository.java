package sprest.user.repositories;

import sprest.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<User, Integer> {

	@Query("FROM User WHERE userName = ?1")
	Optional<User> findByUserName(String userName);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPasswordResetToken(String token);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUserNameIgnoreCase(String userName);
}
