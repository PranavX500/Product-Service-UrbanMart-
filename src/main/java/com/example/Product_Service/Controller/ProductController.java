package com.example.Product_Service.Controller;

import com.example.Product_Service.DTO.ProductDto;
import com.example.Product_Service.Model.Categories;
import com.example.Product_Service.Service.ImageUploadService;
import com.example.Product_Service.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/Product")
public class ProductController {
    @Autowired
    private ImageUploadService imageService;

    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<ProductDto> createProduct(
            @ModelAttribute final ProductDto productDto,
            @RequestPart("image") final MultipartFile image
    ) throws Exception {
        String imageUrl = imageService.uploadImage(image);
        productDto.setImageUrl(imageUrl);

        ProductDto savedProduct = productService.createProduct(productDto);
        return ResponseEntity.ok(savedProduct);
    }

    @GetMapping("/{name}")
    public ResponseEntity<ProductDto> getProductName(@PathVariable final String name) {
        return ResponseEntity.ok(productService.findByProductName(name));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(
            @PathVariable final String category
    ) {
        return ResponseEntity.ok(productService.findAllByCategory(category));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/products/by-ids")
    public ResponseEntity<List<ProductDto>> getProductsByIds(
            @RequestParam final List<Long> ids
    ) {
        List<ProductDto> productDtos = productService.getProductsByIds(ids);
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> productDtos = productService.findByAll();
        return ResponseEntity.ok(productDtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable final Long id,
            @RequestBody final ProductDto dto
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable final Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @GetMapping("/belowPricePaginated")
    public ResponseEntity<Page<ProductDto>> findProductLessThanPrice(
            @RequestParam final Double price,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size
    ) {
        Page<ProductDto> productDtos = productService.findProductLessThanPrice(
                price,
                page,
                size
        );
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/GetTop5/{category}")
    public ResponseEntity<List<ProductDto>> findTop5(@PathVariable final String category) {
        List<ProductDto> productDtos = productService.findTop5(
                Categories.valueOf(category.toUpperCase())
        );
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/BetweenPricePaginated")
    public ResponseEntity<Page<ProductDto>> findProductPriceBetween(
            @RequestParam final Categories categories,
            @RequestParam final Double minprice,
            @RequestParam final Double maxprice,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size
    ) {
        Page<ProductDto> productDtos = productService.findProductPriceBetween(
                categories,
                minprice,
                maxprice,
                page,
                size
        );
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/BrandName/{brand}")
    public ResponseEntity<List<ProductDto>> findByBrand(@PathVariable final String brand) {
        List<ProductDto> productDtos = productService.findByBrand(brand);
        return ResponseEntity.ok(productDtos);
    }
}
