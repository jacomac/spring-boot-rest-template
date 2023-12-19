package sprest.shopping.store;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sprest.user.AppUser;

import java.util.Optional;

@Repository
public interface StoreRepository extends CrudRepository<Store, Integer> {
}
