package com.example.Product_Service.Controller;

import com.example.Product_Service.DTO.ProductDto;
import com.example.Product_Service.Model.Categories;
import com.example.Product_Service.Model.Product;

import com.example.Product_Service.Service.ImageUploadService;
import com.example.Product_Service.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/Product")
public class ProductController {
    @Autowired
    ImageUploadService imageService;

    @Autowired
    ProductService productService;
    @PostMapping("/add")
    public ResponseEntity<ProductDto> createProduct(
            @ModelAttribute ProductDto productDto,
            @RequestPart("image") MultipartFile image
    ) throws Exception {

        String imageUrl =imageService.uploadImage(image);  // Upload to Cloudinary

        productDto.setImageUrl(imageUrl);

        ProductDto saved = productService.Createproduct(productDto);

        return ResponseEntity.ok(saved);
    }





    @GetMapping("/{name}")
    public ResponseEntity<ProductDto>GetProductName(@PathVariable String name){
        return ResponseEntity.ok(productService.findByProductName(name));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>>getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.findAllByCategory(category));
    }
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto>findById(@PathVariable Long id){
        return ResponseEntity.ok(productService.findById(id));
    }
    @GetMapping("/products/by-ids")
    public ResponseEntity<List<ProductDto>> getProductsByIds(@RequestParam List<Long> ids) {
        List<ProductDto> productDTOs = productService.getProductsByIds(ids);
        return ResponseEntity.ok(productDTOs);
    }
    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> productDTOs =productService.findByAll();
        return ResponseEntity.ok(productDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> UpdateProduct(@PathVariable Long id, @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.UpdateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }
    @GetMapping("/belowPricePaginated")
    public ResponseEntity<Page<ProductDto>>findProductLessThenPrice(
            @RequestParam Double price,
            @RequestParam(defaultValue="0")int page,
            @RequestParam(defaultValue = "10") int size){

    Page<ProductDto>productDtos=productService.findProductLessThenPrice(price,page,size);
        return ResponseEntity.ok(productDtos);

    }

    @GetMapping("/GetTop5/{category}")
    public ResponseEntity <List<ProductDto>>fintop5(@PathVariable String category){
        List<ProductDto>productDtos=productService.findBytop5(Categories.valueOf(category));
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/BetweenPricePaginated")
    public ResponseEntity<Page<ProductDto>> findProductPriceBetween(
            @RequestParam Categories categories,
            @RequestParam Double minprice,
            @RequestParam Double maxprice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductDto> productDtos =
                productService.findProductPriceBetween(categories, maxprice, minprice, page, size);

        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/BrandName")
    public ResponseEntity<List<ProductDto>>findByBrand(@PathVariable String brand){
        List<ProductDto>productDtos=productService.findByBrand(brand);
        return ResponseEntity.ok(productDtos);

    }





}
