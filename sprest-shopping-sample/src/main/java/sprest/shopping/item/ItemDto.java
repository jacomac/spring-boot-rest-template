package sprest.shopping.item;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class ItemDto {

    @NotEmpty
    private String name;

    @NotNull
    private int storeId;
}
