package pizza;

import java.util.Date;

public class OrderAccepted extends AbstractEvent {

    private Long storeOrderId;
    private Long storeId;
    private Long orderId;
    private Date acceptDt;
    private String status;

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

