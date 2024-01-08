package pizza;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Entity
@Table(name="Order_table")
public class Order {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long orderId;
    private Long customerId;
    private String pizzaNm;
    private Integer qty;
    private String status;
    private String regionNm;
    private Date orderDt;

    @PrePersist
    public void onPrePersist(){
        //Following code causes dependency to external APIs
        // Req/Res Calling
        boolean bResult = false;

        // mappings goes here
        try {
            bResult = OrderApplication.applicationContext.getBean(pizza.external.StoreService.class).chkOpenYN(this.regionNm);
        } catch(Exception e) {
            e.printStackTrace();
        }

        // 주문가능 (해당 regionNm에 Open된 Store가 있음)
        if (bResult) {
            this.status = "Ordered" ;
        } else {
            this.status = "NoStoreOpened" ;
        }

        this.orderDt = new Date();
    }

    @PostPersist
    public void onPostPersist(){

        if ("Ordered".equals(this.status)) {
            System.out.println("#### PUB :: Ordered : orderId = " + this.orderId);
            Ordered ordered = new Ordered();
            BeanUtils.copyProperties(this, ordered);
            ordered.publishAfterCommit();
        } else if ("NoStoreOpened".equals(this.status)) {
            System.out.println("#### PUB :: OrderRejected : orderId = " + this.orderId);
            OrderRejected orderRejected = new OrderRejected();
            BeanUtils.copyProperties(this, orderRejected);
            orderRejected.publishAfterCommit();
        }
    }

    @PostUpdate
    public void onPostUpdate(){
        if("OrderCancelled".equals(this.status)) {
            System.out.println("#### PUB :: OrderCancelled : orderId = " + this.orderId);
            OrderCancelled orderCancelled = new OrderCancelled();
            BeanUtils.copyProperties(this, orderCancelled);
            orderCancelled.publishAfterCommit();
        } 

        System.out.println("#### PUB :: StatusUpdated : status updated to " + this.status);
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
