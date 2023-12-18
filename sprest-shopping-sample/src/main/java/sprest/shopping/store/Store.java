package sprest.shopping.store;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;
}
