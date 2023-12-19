package sprest.shopping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sprest.api.RequiredAccessRight;
import sprest.shopping.item.Item;
import sprest.shopping.item.ItemDto;
import sprest.shopping.item.ItemRepository;
import sprest.shopping.store.StoreRepository;

import jakarta.validation.Valid;

import java.util.NoSuchElementException;

import static sprest.user.ShoppingRight.values.ACCESS_SHOPPING;

// methods ordered according to CRUD
@Tag(name = "Shopping List Controller", description = "API to maintain a personal shopping list")
@RestController
@RequiredAccessRight(ACCESS_SHOPPING)
@RequestMapping("/items")
public class ShoppingListController {
    private ItemRepository itemRepository;
    private StoreRepository storeRepository;
    private static String MSG_ITEM_NOT_FOUND = "Item not found";

    public ShoppingListController(ItemRepository itemRepository,
                                  StoreRepository storeRepository) {
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
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

    @Operation(summary = "Get a list of items to be bought",
        description = "Get a list of items to be bought chunked into pages")
    @PageableAsQueryParam
    @GetMapping
    public Page<Item> getItems(Pageable page) {
        return itemRepository.findAll(page);
    }

    @Operation(summary = "Get a list of items to be bought",
        description = "Get a list of items to be bought chunked into pages")
    @PageableAsQueryParam
    @GetMapping("/store/{storeId}")
    public Page<Item> getItemsOfStore(@PathVariable("storeId") int storeId, Pageable page) {
        return itemRepository.findByStore_Id(page, storeId);
    }

    @PutMapping("/{id}")
    public Item updateItem (@PathVariable("id") int id, @Valid @RequestBody ItemDto dto) {
        try {
            var item = itemRepository.findById(id).orElseThrow();
            item.copyDto(dto);
            return itemRepository.save(item);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ITEM_NOT_FOUND, e);
        }
    }

    public void deleteItem(@PathVariable("id") int id) {
        try {
            itemRepository.deleteById(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ITEM_NOT_FOUND, e);
        }
    }

}
