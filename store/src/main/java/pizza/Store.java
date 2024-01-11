package pizza;

import javax.persistence.*;

import lombok.Data;

@Entity
@Table(name="Store_table")
@Data
public class Store {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long storeId;
    private String regionNm;
    private Boolean openYN;

}
