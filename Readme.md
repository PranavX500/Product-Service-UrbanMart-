# 🛒 Product Service (Ecommerce Microservices)

The **Product Service** manages all product-related operations in the Ecommerce system, including CRUD, filtering, category-based listing, price-based pagination, and bulk fetching.

---

## 🚀 Tech Stack
- **Spring Boot**
- **Spring Data JPA**
- **MySQL / MongoDB**
- **Eureka Discovery Client**
- **API Gateway**
- **Lombok**
- **Maven**

---

## 📌 Base URL
### **Without Gateway**



---

# 📚 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| **POST** | `/Create` | Create a new product |
| **GET** | `/{name}` | Get product by its name |
| **GET** | `/category/{category}` | Get all products by category |
| **GET** | `/c/{id}` | Get product by ID |
| **GET** | `/products/by-ids?ids=1,2,3` | Get multiple products by list of IDs |
| **GET** | `/all` | Get all products |
| **PUT** | `/{id}` | Update a product by ID |
| **DELETE** | `/{id}` | Delete a product by ID |
| **GET** | `/belowPricePaginated?price=&page=&size=` | Get products below price **with pagination** |
| **GET** | `/BetweenPricePaginated?maxprice=&minprice=&page=&size=` | Get products between min–max price **with pagination** |

---

# 🔎 **Endpoint Details**

### ✅ **1. Create Product**

POST /Product/Create
Body: ProductDto

---

### ✅ **2. Get Product by Name**

GET /Product/{name}


---

### ✅ **3. Get Products by Category**


GET /Product/category/{category}


---

### ✅ **4. Get Product by ID**


GET /Product/c/{id}


---

### ✅ **5. Get Products by Multiple IDs**


GET /Product/products/by-ids?ids=1,2,3,4


---

### ✅ **6. Get All Products**


GET /Product/all


---

### ✅ **7. Update Product**


PUT /Product/{id}
Body: ProductDto


---

### ✅ **8. Delete Product**


DELETE /Product/{id}


---

### ✅ **9. Get Products Below Price (Paginated)**


GET /Product/belowPricePaginated?price=1000&page=0&size=10


---

### ✅ **10. Get Products Between Min–Max Price (Paginated)**


GET /Product/BetweenPricePaginated?maxprice=5000&minprice=2000&page=0&size=10


---



# 🧪 Testing Tools
- Postman  
- Thunder Client  
- Swagger (if enabled)

---

# 👤 Author
**Pranav Sharma**  
Microservices | Spring Boot | Kafka | Redis | SQL

