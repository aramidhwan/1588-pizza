package pizza;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="MyPage_table")
public class MyPage {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long myPageId;
        private Long orderId;
        private String pizzaNm;
        private Integer qty;
        private String status;
        private Date orderDt;
        private Long customerId;


        public Long getMyPageId() {
            return myPageId;
        }

        public void setMyPageId(Long myPageId) {
            this.myPageId = myPageId;
        }
        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
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
        public Date getOrderDt() {
            return orderDt;
        }

        public void setOrderDt(Date orderDt) {
            this.orderDt = orderDt;
        }
        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

}
