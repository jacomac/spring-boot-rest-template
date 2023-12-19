package sprest.shopping.store;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import sprest.shopping.item.Item;
import sprest.user.AppUser;

import java.util.Optional;

@Repository
public interface StoreRepository extends PagingAndSortingRepository<Store, Integer>, CrudRepository<Store, Integer> {
}
