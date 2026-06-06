package com.example.Product_Service.Service;

import com.example.Product_Service.DTO.ProductDto;
import com.example.Product_Service.Exception.BrandNotFound;
import com.example.Product_Service.Exception.InvalidCategoryException;
import com.example.Product_Service.Exception.ProductNotFoundException;
import com.example.Product_Service.Model.Categories;
import com.example.Product_Service.Model.Product;
import com.example.Product_Service.Repositery.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    @CacheEvict(cacheNames = "products", allEntries = true)
    public ProductDto Createproduct(ProductDto productDto) {
        Product product = new Product();
        product.setProductName(productDto.getProductName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setBrand(productDto.getBrand());
        product.setImageUrl(productDto.getImageUrl());
        product.setCategories(productDto.getCategories());

        Product saved = productRepo.save(product);
        return MapToDto(saved);
    }


    private ProductDto MapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setBrand(product.getBrand());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategories(product.getCategories());
        return dto;
    }


    @Cacheable(cacheNames = "products", key = "'brand:' + #brand")
    public List<ProductDto> findByBrand(String brand) {
        List<Product> products = productRepo.findByBrand(brand);

        if (products.isEmpty()) {
            throw new BrandNotFound("No products found for brand: " + brand);
        }

        return products.stream().map(this::MapToDto).toList();
    }


    @Cacheable(cacheNames = "products", key = "'all'")
    public List<ProductDto> findByAll() {
        List<Product> products = productRepo.findAll();

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No products available");
        }

        return products.stream().map(this::MapToDto).toList();
    }


    @Cacheable(cacheNames = "products", key = "'top5:' + #categories.name()")
    public List<ProductDto> findBytop5(Categories categories) {
        List<Product> products = productRepo.findTop5ByCategories(categories);

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No top products found for category: " + categories);
        }

        return products.stream().map(this::MapToDto).toList();
    }


    @Cacheable(cacheNames = "products", key = "'name:' + #productName")
    public ProductDto findByProductName(String productName) {
        Product product = productRepo.findByProductName(productName)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productName));
        return MapToDto(product);
    }


    @Cacheable(cacheNames = "products", key = "'id:' + #id")
    public ProductDto findById(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return MapToDto(product);
    }


    @Cacheable(cacheNames = "products", key = "'ids:' + #ids.toString()")
    public List<ProductDto> getProductsByIds(List<Long> ids) {
        List<Product> products = productRepo.findByIdIn(ids);

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No products found for ids: " + ids);
        }

        return products.stream().map(this::MapToDto).toList();
    }


    @CacheEvict(cacheNames = "products", allEntries = true)
    public ProductDto UpdateProduct(Long id, ProductDto productDto) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        if (productDto.getProductName() != null) product.setProductName(productDto.getProductName());
        if (productDto.getDescription() != null) product.setDescription(productDto.getDescription());
        if (productDto.getPrice() != null) product.setPrice(productDto.getPrice());
        if (productDto.getCategories() != null) product.setCategories(productDto.getCategories());

        return MapToDto(productRepo.save(product));
    }


    @CacheEvict(cacheNames = "products", allEntries = true)
    public String deleteProduct(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        productRepo.delete(product);
        return "Product deleted successfully with ID: " + id;
    }


    @Cacheable(cacheNames = "products", key = "'category:' + #category.toUpperCase()")
    public List<ProductDto> findAllByCategory(String category) {
        Categories categoriesEnum;
        try {
            categoriesEnum = Categories.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCategoryException("Invalid category: " + category);
        }

        List<Product> products = productRepo.findAllByCategories(categoriesEnum);

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No products found in category: " + category);
        }

        return products.stream().map(this::MapToDto).toList();
    }


    public Page<ProductDto> findProductLessThenPrice(Double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        Page<Product> products = productRepo.findProductsBelowPrice(maxPrice, pageable);

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No products found below price: " + maxPrice);
        }

        return products.map(this::MapToDto);
    }


    public Page<ProductDto> findProductPriceBetween(
            Categories categories,
            Double minPrice,
            Double maxPrice,
            int page,
            int size
    ) {
        double lowerPrice = Math.min(minPrice, maxPrice);
        double upperPrice = Math.max(minPrice, maxPrice);
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());

        Page<Product> products = productRepo.findProductBetweenPrice(
                categories, lowerPrice, upperPrice, pageable
        );

        if (products.isEmpty()) {
            throw new ProductNotFoundException(
                    "No products found in " + categories + " between " + lowerPrice + " and " + upperPrice
            );
        }

        return products.map(this::MapToDto);
    }
}
