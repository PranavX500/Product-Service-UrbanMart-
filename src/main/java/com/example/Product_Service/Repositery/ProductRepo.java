package com.example.Product_Service.Repositery;

import com.example.Product_Service.Model.Categories;
import com.example.Product_Service.Model.Product;
import org.springframework.data.domain.Page;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product,Long> {
    Optional<Product>findByProductName(String productName);
    Optional<Product>findByCategories(Categories categories);
    Optional<Product>findById(Long id);
    List<Product>findAllByCategories(Categories categories);
    List<Product>findByIdIn(List<Long>id);
    List<Product>findByBrand(String brand);
    List<Product> findTop5ByCategories(Categories categories);


    @Query("SELECT p FROM Product p WHERE p.price < :maxPrice")
    Page<Product> findProductsBelowPrice(@Param("maxPrice") Double maxPrice, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.categories = :categories AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProductBetweenPrice(
            @Param("categories") Categories categories,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );


}
