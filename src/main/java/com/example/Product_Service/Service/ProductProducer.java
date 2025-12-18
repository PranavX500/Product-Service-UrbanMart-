package com.example.Product_Service.Service;

import com.example.Product_Service.DTO.ProductResponseEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductProducer {

    private final KafkaTemplate<String, ProductResponseEvent> kafkaTemplate;

    public ProductProducer(KafkaTemplate<String, ProductResponseEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendProductIds(ProductResponseEvent event) {
        System.out.print(event);
        kafkaTemplate.send("product-success-topic", event);
        System.out.println("Sent event to Kafka: " + event);
    }
}
