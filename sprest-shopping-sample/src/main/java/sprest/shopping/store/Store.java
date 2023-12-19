package sprest.shopping.store;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import jakarta.persistence.*;
import sprest.user.AppUser;

@Data
@Entity
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column
    private String name;

}
