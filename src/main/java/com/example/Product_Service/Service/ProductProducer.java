package com.example.Product_Service.Service;

import com.example.Product_Service.DTO.ProductResponseEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductProducer {

    private final KafkaTemplate<String, ProductResponseEvent> kafkaTemplate;

    public ProductProducer(final KafkaTemplate<String, ProductResponseEvent> template) {
        this.kafkaTemplate = template;
    }

    public void sendProductIds(final ProductResponseEvent event) {
        kafkaTemplate.send("product-success-topic", event);
    }
}
