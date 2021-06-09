package pizza;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.Date;

@Entity
@Table(name="StoreOrder_table")
public class StoreOrder {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long storeOrderId;
    private Long storeId;
    private Long orderId;
    private Date acceptDt;
    private String status;

    @PrePersist
    public void onPrePersist(){
        this.acceptDt = new Date() ;
    }

    @PostPersist
    public void onPostPersist(){
        OrderAccepted orderAccepted = new OrderAccepted();
        BeanUtils.copyProperties(this, orderAccepted);
        orderAccepted.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        if ("Cooked".equals(this.status)) {
            Cooked cooked = new Cooked();
            BeanUtils.copyProperties(this, cooked);
            cooked.publishAfterCommit();
        } 
    }

    public Long getStoreOrderId() {
        return storeOrderId;
    }

    public void setStoreOrderId(Long storeOrderId) {
        this.storeOrderId = storeOrderId;
    }
    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Date getAcceptDt() {
        return acceptDt;
    }

    public void setAcceptDt(Date acceptDt) {
        this.acceptDt = acceptDt;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
