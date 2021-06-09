package pizza;

import javax.persistence.*;

@Entity
@Table(name="Store_table")
public class Store {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long storeId;
    private String regionNm;
    private Boolean openYN;

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
    public String getRegionNm() {
        return regionNm;
    }

    public void setRegionNm(String regionNm) {
        this.regionNm = regionNm;
    }
    public Boolean getOpenYN() {
        return openYN;
    }

    public void setOpenYN(Boolean openYn) {
        this.openYN = openYn;
    }
}
