package sprest.user.repositories;

import sprest.user.UserAuthority;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface UserAuthorityRepository extends PagingAndSortingRepository<UserAuthority, Integer> {
    @Query("SELECT h FROM UserAuthority h WHERE authority = ?1")
    Optional<UserAuthority> findByAuthority(String authority);
}
