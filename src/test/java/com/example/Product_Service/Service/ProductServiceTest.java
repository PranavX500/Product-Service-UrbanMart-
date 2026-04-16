package com.example.Product_Service.Service;

import com.example.Product_Service.DTO.ProductDto;
import com.example.Product_Service.Exception.BrandNotFound;
import com.example.Product_Service.Exception.InvalidCategoryException;
import com.example.Product_Service.Exception.ProductNotFoundException;
import com.example.Product_Service.Model.Categories;
import com.example.Product_Service.Model.Product;
import com.example.Product_Service.Repositery.ProductRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProductReturnsMappedDto() {
        ProductDto request = productDto(null, "Phone", 49999.0, "Apple", Categories.ELECTRONICS);
        Product savedProduct = product(1L, "Phone", 49999.0, "Apple", Categories.ELECTRONICS);

        when(productRepo.save(any(Product.class))).thenReturn(savedProduct);

        ProductDto result = productService.Createproduct(request);

        assertEquals(1L, result.getProductId());
        assertEquals("Phone", result.getProductName());
        assertEquals(49999.0, result.getPrice());
        assertEquals("Apple", result.getBrand());
        assertEquals(Categories.ELECTRONICS, result.getCategories());
        verify(productRepo).save(any(Product.class));
    }

    @Test
    void findByBrandReturnsProductsWhenFound() {
        when(productRepo.findByBrand("Nike"))
                .thenReturn(List.of(product(1L, "Shoes", 2999.0, "Nike", Categories.FOOTWEAR)));

        List<ProductDto> result = productService.findByBrand("Nike");

        assertEquals(1, result.size());
        assertEquals("Shoes", result.get(0).getProductName());
        assertEquals("Nike", result.get(0).getBrand());
    }

    @Test
    void findByBrandThrowsWhenNothingExists() {
        when(productRepo.findByBrand("Nike")).thenReturn(List.of());

        BrandNotFound exception = assertThrows(
                BrandNotFound.class,
                () -> productService.findByBrand("Nike")
        );

        assertEquals("No products found for brand: Nike", exception.getMessage());
    }

    @Test
    void findByIdReturnsProductWhenPresent() {
        when(productRepo.findById(10L))
                .thenReturn(Optional.of(product(10L, "Laptop", 75000.0, "Dell", Categories.ELECTRONICS)));

        ProductDto result = productService.findById(10L);

        assertEquals(10L, result.getProductId());
        assertEquals("Laptop", result.getProductName());
        assertEquals("Dell", result.getBrand());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(productRepo.findById(10L)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.findById(10L)
        );

        assertEquals("Product not found with id: 10", exception.getMessage());
    }

    @Test
    void updateProductOnlyChangesProvidedFields() {
        Product existingProduct = product(3L, "Old Phone", 20000.0, "Samsung", Categories.ELECTRONICS);
        ProductDto updateRequest = new ProductDto();
        updateRequest.setProductName("New Phone");
        updateRequest.setPrice(25000.0);

        when(productRepo.findById(3L)).thenReturn(Optional.of(existingProduct));
        when(productRepo.save(existingProduct)).thenReturn(existingProduct);

        ProductDto result = productService.UpdateProduct(3L, updateRequest);

        assertEquals("New Phone", result.getProductName());
        assertEquals(25000.0, result.getPrice());
        assertEquals("Samsung", result.getBrand());
        assertEquals(Categories.ELECTRONICS, result.getCategories());
        verify(productRepo).save(existingProduct);
    }

    @Test
    void deleteProductDeletesExistingEntity() {
        Product existingProduct = product(7L, "Book", 499.0, "Penguin", Categories.BOOKS);
        when(productRepo.findById(7L)).thenReturn(Optional.of(existingProduct));

        String result = productService.deleteProduct(7L);

        assertEquals("Product deleted successfully with ID: 7", result);
        verify(productRepo).delete(existingProduct);
    }

    @Test
    void findAllByCategoryThrowsForInvalidCategory() {
        InvalidCategoryException exception = assertThrows(
                InvalidCategoryException.class,
                () -> productService.findAllByCategory("invalid-category")
        );

        assertEquals("Invalid category: invalid-category", exception.getMessage());
        verify(productRepo, never()).findAllByCategories(any());
    }

    @Test
    void findAllByCategoryReturnsProductsForValidCategory() {
        when(productRepo.findAllByCategories(Categories.ELECTRONICS))
                .thenReturn(List.of(product(5L, "Camera", 45000.0, "Sony", Categories.ELECTRONICS)));

        List<ProductDto> result = productService.findAllByCategory("electronics");

        assertEquals(1, result.size());
        assertEquals("Camera", result.get(0).getProductName());
        assertEquals(Categories.ELECTRONICS, result.get(0).getCategories());
    }

    @Test
    void findProductLessThenPriceReturnsPagedDtos() {
        Page<Product> page = new PageImpl<>(
                List.of(product(2L, "Mouse", 799.0, "Logitech", Categories.COMPUTER_ACCESSORIES))
        );
        when(productRepo.findProductsBelowPrice(eq(1000.0), any(Pageable.class))).thenReturn(page);

        Page<ProductDto> result = productService.findProductLessThenPrice(1000.0, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals("Mouse", result.getContent().get(0).getProductName());
    }

    @Test
    void findProductPriceBetweenThrowsWhenPageIsEmpty() {
        when(productRepo.findProductBetweenPrice(
                eq(Categories.ELECTRONICS),
                eq(1000.0),
                eq(2000.0),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.findProductPriceBetween(Categories.ELECTRONICS, 1000.0, 2000.0, 0, 5)
        );

        assertTrue(exception.getMessage().contains("No products found in ELECTRONICS between 1000.0 and 2000.0"));
    }

    private Product product(Long id, String name, Double price, String brand, Categories category) {
        Product product = new Product();
        product.setId(id);
        product.setProductName(name);
        product.setPrice(price);
        product.setQuantity(10);
        product.setDescription(name + " description");
        product.setBrand(brand);
        product.setImageUrl("image-url");
        product.setCategories(category);
        return product;
    }

    private ProductDto productDto(Long id, String name, Double price, String brand, Categories category) {
        ProductDto dto = new ProductDto();
        dto.setProductId(id);
        dto.setProductName(name);
        dto.setPrice(price);
        dto.setQuantity(10);
        dto.setDescription(name + " description");
        dto.setBrand(brand);
        dto.setImageUrl("image-url");
        dto.setCategories(category);
        return dto;
    }
}
