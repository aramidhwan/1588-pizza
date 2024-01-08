package pizza;

import pizza.config.kafka.KafkaProcessor;

import java.util.Optional;

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

        Optional<Delivery> deliveryOptional = deliveryRepository.findByOrderId(cooked.getOrderId());

        // 배달 상태 업데이트
        if (deliveryOptional.isPresent()) {
            Delivery delivery = deliveryOptional.get();
            delivery.setStatus("DeliveryStart");
            deliveryRepository.save(delivery);
        // 배달접수
        } else {
            Delivery delivery = new Delivery();
            BeanUtils.copyProperties(cooked, delivery);
            delivery.setStatus("DeliveryStart");
            deliveryRepository.save(delivery);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCancelled_CancelDelivery(@Payload OrderCancelled orderCancelled){

        if(!orderCancelled.validate()) return;

        System.out.println("\n\n##### listener CancelDelivery : " + orderCancelled.getOrderId());

        // 배달취소
        Optional<Delivery> deliveryOptional = deliveryRepository.findByOrderId(orderCancelled.getOrderId());

        if (deliveryOptional.isPresent()) {
            Delivery delivery = deliveryOptional.get();
            delivery.setStatus("OrderCancelled");
            deliveryRepository.save(delivery);
        }
    }
}
