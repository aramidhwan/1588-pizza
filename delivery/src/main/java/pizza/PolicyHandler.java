package pizza;

import pizza.config.kafka.KafkaProcessor;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired
    DeliveryRepository deliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCooked_DeliveryAccept(@Payload Cooked cooked){

        if(!cooked.validate()) return;

        System.out.println("\n\n##### listener DeliveryAccept : " + cooked.getOrderId());

        // 배달접수
        Delivery delivery = new Delivery();
        BeanUtils.copyProperties(cooked, delivery);
        delivery.setStatus("DeliveryStart");
        deliveryRepository.save(delivery);
    }
}
