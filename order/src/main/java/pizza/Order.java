package pizza;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Order_table")
public class Order {

    private Long orderId;
    private Long customerId;
    private String pizzaNm;
    private Integer qty;
    private String status;
    private String regionNm;
    private Date orderDt;

    @PostPersist
    public void onPostPersist(){
        Ordered ordered = new Ordered();
        BeanUtils.copyProperties(this, ordered);
        ordered.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        pizza.external.Store store = new pizza.external.Store();
        // mappings goes here
        Application.applicationContext.getBean(pizza.external.StoreService.class)
            .chkOpenYn(store);


        OrderCancelled orderCancelled = new OrderCancelled();
        BeanUtils.copyProperties(this, orderCancelled);
        orderCancelled.publishAfterCommit();


        StatusUpdated statusUpdated = new StatusUpdated();
        BeanUtils.copyProperties(this, statusUpdated);
        statusUpdated.publishAfterCommit();


    }


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public String getPizzaNm() {
        return pizzaNm;
    }

    public void setPizzaNm(String pizzaNm) {
        this.pizzaNm = pizzaNm;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getRegionNm() {
        return regionNm;
    }

    public void setRegionNm(String regionNm) {
        this.regionNm = regionNm;
    }
    public Date getOrderDt() {
        return orderDt;
    }

    public void setOrderDt(Date orderDt) {
        this.orderDt = orderDt;
    }




}
