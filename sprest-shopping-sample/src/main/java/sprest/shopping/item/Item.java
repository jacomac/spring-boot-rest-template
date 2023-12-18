package sprest.shopping.item;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import sprest.shopping.store.Store;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;
}
