package pizza;

import pizza.config.kafka.KafkaProcessor;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired
    StoreRepository storeRepository;
    @Autowired 
    StoreOrderRepository storeOrderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_OrderAccept(@Payload Ordered ordered){

        if(!ordered.validate()) return;

        System.out.println("\n\n##### listener OrderAccept : " + ordered.getOrderId());

        // 주문접수
        List<Store> storeList = storeRepository.findByRegionNmOpenYN(ordered.getRegionNm(), Boolean.valueOf(true));
        int openStoreCnt = storeList.size();

        if (openStoreCnt > 0) {
            int random = new Random().nextInt(openStoreCnt-1) ;

            StoreOrder storeOrder = new StoreOrder();
            BeanUtils.copyProperties(ordered, storeOrder);
            storeOrder.setStoreId(storeList.get(random).getStoreId());
            storeOrder.setStatus("OrderAccepted");
            storeOrderRepository.save(storeOrder);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCancelled_CancelAccepted(@Payload OrderCancelled orderCancelled){

        if(!orderCancelled.validate()) return;

        System.out.println("\n\n##### listener CancelAccepted : " + orderCancelled.getOrderId());

        // 주문취소접수
        Optional<StoreOrder> storeOrderOptional = storeOrderRepository.findByOrderId(orderCancelled.getOrderId());

        if (storeOrderOptional.isPresent()) {
            StoreOrder storeOrder = storeOrderOptional.get();
            storeOrder.setStatus("StoreAcceptCancelled");
            storeOrderRepository.save(storeOrder);
        }
    }
}
