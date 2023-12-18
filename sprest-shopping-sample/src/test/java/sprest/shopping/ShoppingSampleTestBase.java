package sprest.shopping;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import sprest.ControllerTestBase;
import sprest.shopping.item.Item;
import sprest.shopping.item.ItemRepository;
import sprest.shopping.store.Store;
import sprest.shopping.store.StoreRepository;

import java.util.Date;
import java.util.List;

public class ShoppingSampleTestBase extends ControllerTestBase {

    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private ItemRepository itemRepository;

    protected Item item;
    protected Store store;
    protected Integer otherStoreId;

    @BeforeEach
    public void setup() {
        super.setup();
        setUpTestStore();
        setUpItem();
    }

    private void setUpTestStore() {
        var s = new Store();
        s.setName("Autumn Store");

        this.store = storeRepository.save(s);
    }

    private void setUpItem() {
        var i = new Item();
        i.setName("Coat");
        i.setCreationDate(new Date());
        i.setStore(store);

        this.item = itemRepository.save(i);
    }

    protected void mockMoreItems() {
        var itemNames = List.of("Boots", "Jeans", "Hat", "Gloves");
        var items = itemNames.stream().map(name -> {
            var item = new Item();
            item.setName(name);
            item.setCreationDate(new Date());
            item.setStore(store);
            return item;
        }).toList();

        itemRepository.saveAll(items);
    }

    protected void mockMoreStores() {
        var storeNames = List.of("Spring Store", "Summer Store");
        var stores = storeNames.stream().map(name -> {
            var store = new Store();
            store.setName(name);
            return store;
        }).toList();

        storeRepository.saveAll(stores).forEach(store -> {
            var item = new Item();
            item.setName(store.getName() + " T-shirt");
            item.setStore(store);
            itemRepository.save(item);
        });
    }
}
