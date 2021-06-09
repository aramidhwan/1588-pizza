
package pizza;

public class DeliveryStarted extends AbstractEvent {

    private Long id;
    private Long deliveryId;
    private Long orderId;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return deliveryId;
    }

    public void setId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

