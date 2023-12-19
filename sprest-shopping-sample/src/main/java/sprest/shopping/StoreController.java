package sprest.shopping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sprest.api.RequiredAccessRight;
import sprest.shopping.store.Store;
import sprest.shopping.store.StoreDto;
import sprest.shopping.store.StoreRepository;

import java.util.NoSuchElementException;

import static sprest.user.ShoppingRight.values.ACCESS_SHOPPING;

// methods ordered according to CRUD
@Tag(name = "Store Controller", description = "API to maintain stores")
@RestController
@RequiredAccessRight(ACCESS_SHOPPING)
@RequestMapping("/stores")
public class StoreController {
    private StoreRepository storeRepository;
    private static String MSG_STORE_NOT_FOUND = "Store not found";

    public StoreController(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @PostMapping
    public Store createStore(@Valid @RequestBody StoreDto dto) {
        var store = new Store();
        store.setName(dto.getName());
        return storeRepository.save(store);
    }

    @GetMapping("/{id}")
    public Store getStore(@PathVariable("id") int id) {
        var store = storeRepository.findById(id);
        if (store.isPresent()) {
            return store.get();
        } else {
            var msg = String.format("store %s could not be found", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }
    }

    @Operation(summary = "Get a list of stores where to buy",
        description = "Get a list of stores where to buy, chunked into pages")
    @PageableAsQueryParam
    @GetMapping
    public Page<Store> getStores(Pageable page) {
        return storeRepository.findAll(page);
    }

    @PutMapping("/{id}")
    public Store updateStore (@PathVariable("id") int id, @Valid @RequestBody StoreDto dto) {
        try {
            var store = storeRepository.findById(id).orElseThrow();
            store.copyDto(dto);
            return storeRepository.save(store);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_STORE_NOT_FOUND, e);
        }
    }

    public void deletestore(@PathVariable("id") int id) {
        try {
            storeRepository.deleteById(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_STORE_NOT_FOUND, e);
        }
    }
}
