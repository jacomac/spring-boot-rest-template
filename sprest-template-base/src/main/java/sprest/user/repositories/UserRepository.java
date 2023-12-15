package sprest.user.repositories;

import org.springframework.data.repository.CrudRepository;
import sprest.user.AppUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<AppUser, Integer>, CrudRepository <AppUser, Integer> {

	@Query("FROM AppUser WHERE userName = ?1")
	Optional<AppUser> findByUserName(String userName);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByPasswordResetToken(String token);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUserNameIgnoreCase(String userName);
}
