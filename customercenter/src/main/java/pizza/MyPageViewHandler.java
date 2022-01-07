package pizza;

import pizza.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyPageViewHandler {

    @Autowired
    private MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrdered_then_CREATE_1 (@Payload Ordered ordered) {
        try {

            if (!ordered.validate()) return;

            // view 객체 생성
            MyPage myPage = new MyPage();
            // view 객체에 이벤트의 Value 를 set 함
            myPage.setOrderId(ordered.getOrderId());
            myPage.setPizzaNm(ordered.getPizzaNm());
            myPage.setQty(ordered.getQty());
            myPage.setStatus(ordered.getStatus());
            myPage.setOrderDt(ordered.getOrderDt());
            myPage.setCustomerId(ordered.getCustomerId());
            // view 레파지 토리에 save
            myPageRepository.save(myPage);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderRejected_then_CREATE_2 (@Payload OrderRejected orderRejected) {
        try {

            if (!orderRejected.validate()) return;

            // view 객체 생성
            MyPage myPage = new MyPage();
            // view 객체에 이벤트의 Value 를 set 함
            myPage.setOrderId(orderRejected.getOrderId());
            myPage.setPizzaNm(orderRejected.getPizzaNm());
            myPage.setQty(orderRejected.getQty());
            myPage.setStatus(orderRejected.getStatus());
            myPage.setOrderDt(orderRejected.getOrderDt());
            myPage.setCustomerId(orderRejected.getCustomerId());
            // view 레파지 토리에 save
            myPageRepository.save(myPage);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenStatusUpdated_then_UPDATE_1(@Payload StatusUpdated statusUpdated) {
        try {
            if (!statusUpdated.validate()) return;
                // view 객체 조회
            List<MyPage> myPageList = myPageRepository.findByOrderId(statusUpdated.getOrderId());
            for(MyPage myPage : myPageList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                myPage.setStatus(statusUpdated.getStatus());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCancelled_then_UPDATE_2(@Payload OrderCancelled orderCancelled) {
        try {
            if (!orderCancelled.validate()) return;
                // view 객체 조회
            List<MyPage> myPageList = myPageRepository.findByOrderId(orderCancelled.getOrderId());
            for(MyPage myPage : myPageList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                myPage.setStatus(orderCancelled.getStatus());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}