package sprest.shopping.store;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column
    private String name;

    public void copyDto(StoreDto dto) {
        setName(dto.getName());
    }

}
