package sprest.user.repositories;

import org.springframework.data.repository.CrudRepository;
import sprest.user.AccessRight;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface AccessRightRepository extends PagingAndSortingRepository<AccessRight, Integer>, CrudRepository<AccessRight, Integer> {

    Optional<AccessRight> findByName(String rightName);
}
