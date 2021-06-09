package pizza;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Store_table")
public class Store {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long storeId;
    private String regionNm;
    private Boolean openYn;


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
    public Boolean getOpenYn() {
        return openYn;
    }

    public void setOpenYn(Boolean openYn) {
        this.openYn = openYn;
    }




}
