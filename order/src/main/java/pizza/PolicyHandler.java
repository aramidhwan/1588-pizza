package pizza;

import pizza.config.kafka.KafkaProcessor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired
    OrderRepository orderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCooked_UpdateStatus(@Payload Cooked cooked){

        if(!cooked.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + cooked.getOrderId() + ", " + cooked.getStatus());
        Optional<Order> orderOptional = orderRepository.findByOrderId(cooked.getOrderId());
        if ( orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(cooked.getStatus());
            orderRepository.save(order);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderAccepted_UpdateStatus(@Payload OrderAccepted orderAccepted){

        if(!orderAccepted.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + orderAccepted.getOrderId() + ", " + orderAccepted.getStatus());
        Optional<Order> orderOptional = orderRepository.findByOrderId(orderAccepted.getOrderId());
        if ( orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(orderAccepted.getStatus());
            orderRepository.save(order);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_UpdateStatus(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + deliveryStarted.getOrderId() + ", " + deliveryStarted.getStatus());
        Optional<Order> orderOptional = orderRepository.findByOrderId(deliveryStarted.getOrderId());
        if ( orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(deliveryStarted.getStatus());
            orderRepository.save(order);
        }
    }
}
