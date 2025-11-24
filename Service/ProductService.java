package com.example.Product_Service.Service;

import com.example.Product_Service.DTO.ListSuccessEvent;
import com.example.Product_Service.DTO.ProductDto;
import com.example.Product_Service.Model.Categories;
import com.example.Product_Service.Model.Product;
import com.example.Product_Service.Repositery.ProductRepo;

import lombok.Builder;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class ProductService {
    @Autowired
    ProductRepo productRepo;
   public ProductDto Createproduct( ProductDto productDto){

       Product product = new Product();
       product.setId(product.getId());
       product.setProductName(productDto.getProductName());
       product.setDescription(productDto.getDescription());
       product.setPrice(productDto.getPrice());


       product.setQuantity(productDto.getQuantity());
       product.setBrand(productDto.getBrand());
       product.setImageUrl(productDto.getImageUrl());
       product.setCategories(productDto.getCategories());

       Product savedProduct=productRepo.save(product);


       return  MapToDto(savedProduct);



   }
   public ProductDto MapToDto(Product product){

       ProductDto responseDto = new ProductDto();
       responseDto.setProductId(product.getId());
       responseDto.setProductName(product.getProductName());
       responseDto.setDescription(product.getDescription());
       responseDto.setPrice(product.getPrice());

       responseDto.setQuantity(product.getQuantity());
       responseDto.setBrand(product.getBrand());
       responseDto.setImageUrl(product.getImageUrl());
       responseDto.setCategories(product.getCategories());


       return responseDto;

   }



public List<ProductDto>findByBrand(String brand){
       List<Product>products=productRepo.findByBrand(brand);
    return products.stream()
            .map(this:: MapToDto)
            .toList();
}

   public List<ProductDto>findByAll(){
      List<Product> product=productRepo.findAll();
       return product.stream()
               .map(this:: MapToDto)  // use your existing method
               .toList();

   }
   public List<ProductDto> findBytop5(Categories categories){
       List<Product>products=productRepo.findTop5ByCategories(categories);
       return  products.stream()
               .map(this:: MapToDto)  // use your existing method
               .toList();

   }

    public ProductDto findByProductName(String productName ){
       Product product=productRepo.findByProductName(productName).orElseThrow(()->new RuntimeException("Product not found"));
       return MapToDto(product);
   }
   public  ProductDto findById(Long id){
       Product product=productRepo.findById(id).orElseThrow(()->new RuntimeException("Id not found"));
       return MapToDto(product);
   }

    public List<ProductDto> getProductsByIds(List<Long> ids) {
        return productRepo.findByIdIn(ids)
                .stream()
                .map(this::MapToDto)
                .collect(Collectors.toList());
    }








    public ProductDto UpdateProduct(Long id,ProductDto productDto){
       Product product=productRepo.findById(id).orElseThrow(()->new RuntimeException("id not found"));
       if (productDto.getProductName() != null) product.setProductName(productDto.getProductName());
       if (productDto.getDescription() != null) product.setDescription(productDto.getDescription());
       if (productDto.getPrice() != null) product.setPrice(productDto.getPrice());
       if (productDto.getQuantity() > 0) product.setQuantity(productDto.getQuantity());

       if (productDto.getCategories() != null) product.setCategories(productDto.getCategories());

       Product updatedProduct = productRepo.save(product);
       return MapToDto(updatedProduct);

   }
    public String deleteProduct(Long id) {
        productRepo.deleteById(id);
        return "Product deleted successfully with ID: " + id;
    }
    public List<ProductDto> findAllByCategory(String category) {
        Categories categoriesEnum;
        try {
            categoriesEnum = Categories.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + category);
        }

        List<Product> products = productRepo.findAllByCategories(categoriesEnum);
        if (products.isEmpty()) {
            throw new RuntimeException("No products found in category: " + category);
        }

        return products.stream()
                .map(this::MapToDto)
                .collect(Collectors.toList());
    }

    public Page<ProductDto> findProductLessThenPrice(Double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());

        Page<Product> products = productRepo.findProductsBelowPrice(maxPrice, pageable);
        return products.map(this::MapToDto);
    }
    public  Page<ProductDto>findProductPriceBetween(Double maxPrice,Double minPrice,int page,int size){
       Pageable pageable=PageRequest.of(page,size,Sort.by("price").ascending());
       Page<Product>products=productRepo.findProductBetweenPrice(maxPrice,minPrice,pageable);
        return products.map(this::MapToDto);

    }



}
