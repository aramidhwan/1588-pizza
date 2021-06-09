package pizza;

import pizza.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired StoreRepository storeRepository;
    @Autowired StoreOrderRepository storeOrderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_OrderAccept(@Payload Ordered ordered){

        if(!ordered.validate()) return;

        System.out.println("\n\n##### listener OrderAccept : " + ordered.toJson() + "\n\n");

        // Sample Logic //
        Store store = new Store();
        storeRepository.save(store);
        StoreOrder storeOrder = new StoreOrder();
        storeOrderRepository.save(storeOrder);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCancelled_CancelAccepted(@Payload OrderCancelled orderCancelled){

        if(!orderCancelled.validate()) return;

        System.out.println("\n\n##### listener CancelAccepted : " + orderCancelled.toJson() + "\n\n");

        // Sample Logic //
        Store store = new Store();
        storeRepository.save(store);
        StoreOrder storeOrder = new StoreOrder();
        storeOrderRepository.save(storeOrder);
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
