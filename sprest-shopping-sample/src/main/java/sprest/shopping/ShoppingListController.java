package sprest.shopping;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sprest.api.RequiredAccessRight;
import sprest.shopping.item.Item;
import sprest.shopping.item.ItemDto;
import sprest.shopping.item.ItemRepository;
import sprest.shopping.store.StoreRepository;

import jakarta.validation.Valid;

import static sprest.user.ShoppingRight.values.ACCESS_SHOPPING;

@Tag(name = "Shopping List Controller", description = "API to maintain a personal shopping list")
@RestController
@RequiredAccessRight(ACCESS_SHOPPING)
@RequestMapping("/items")
public class ShoppingListController {
    private ItemRepository itemRepository;
    private StoreRepository storeRepository;

    public ShoppingListController(ItemRepository itemRepository,
                                  StoreRepository storeRepository) {
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
    }

    @GetMapping("/{id}")
    public Item getItem(@PathVariable("id") int id) {
        var item = itemRepository.findById(id);
        if (item.isPresent()) {
            return item.get();
        } else {
            var msg = String.format("item %s could not be found", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }
    }

    @PostMapping
    public Item createItem (@Valid @RequestBody ItemDto dto) {
        var store = storeRepository.findById(dto.getStoreId());
        if (store.isEmpty()) {
            var msg = String.format("store %s could not be found", dto.getStoreId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        } else {
            var item = new Item();
            item.setName(dto.getName());
            item.setStore(store.get());
            return itemRepository.save(item);
        }
    }
}
