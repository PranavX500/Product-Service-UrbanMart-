package com.example.Product_Service.Service;

import com.example.Product_Service.DTO.ListSuccessEvent;
import com.example.Product_Service.DTO.ProductDto;
import com.example.Product_Service.DTO.ProductResponseEvent;
import com.example.Product_Service.Model.Product;
import com.example.Product_Service.Repositery.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductConsumer {
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    ProductProducer productProducer;
    @KafkaListener(topics ="Productids",groupId ="product-group")
    public void handleIds(ListSuccessEvent event){
      List<Long> ids=event.getIds();
      List<Product>productList=productRepo.findByIdIn(ids);
      List<ProductDto>products=productList.stream()
              .map(product -> new ProductDto(
                      product.getId(),
                      product.getProductName(),
                      product.getPrice(),
                      product.getQuantity(),
                      product.getDescription(),
                      product.getBrand(),
                      product.getImageUrl(),
                      product.getCategories()




              ))
              .toList();
        ProductResponseEvent responseEvent = new ProductResponseEvent(products);

        responseEvent.setRequestId(event.getRequestId());
        responseEvent.setEmailId(event.getEmailId());
        responseEvent.setUserId(event.getUserId());
        System.out.print((responseEvent));

        productProducer.sendProductIds(responseEvent);
    }




}
