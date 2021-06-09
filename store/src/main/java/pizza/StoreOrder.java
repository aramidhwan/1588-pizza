package pizza;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="StoreOrder_table")
public class StoreOrder {

    private Long storeOrderId;
    private Long storeId;
    private Long orderId;
    private Date acceptDt;
    private String status;

    @PostPersist
    public void onPostPersist(){
        Cooked cooked = new Cooked();
        BeanUtils.copyProperties(this, cooked);
        cooked.publishAfterCommit();


        OrderAccepted orderAccepted = new OrderAccepted();
        BeanUtils.copyProperties(this, orderAccepted);
        orderAccepted.publishAfterCommit();


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
