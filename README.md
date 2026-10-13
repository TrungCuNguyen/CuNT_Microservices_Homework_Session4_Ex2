# Microservices Project (Session 4)

Dự án gồm **Discovery Server** và **3 Microservices** đã được tích hợp Eureka Registry:
- **`discovery-server`** (Cổng: **8761**, Eureka Server - "Cuốn danh bạ trung tâm")
- **`customer-service`** (Cổng: **8081**, Database: `customer_db`, Eureka Client)
- **`product-service`** (Cổng: **8082**, Database: `product_db`, Eureka Client)
- **`order-service`** (Cổng: **8083**, Database: `order_db`, Eureka Client)

---

## 1. Discovery Server (`discovery-server`)
- **Cổng:** `8761`
- **Dashboard:** Truy cập trình duyệt tại: `http://localhost:8761`
- **Annotation:** `@EnableEurekaServer`
- **Cấu hình `application.properties`:**
  ```properties
  server.port=8761
  spring.application.name=discovery-server
  eureka.instance.hostname=localhost
  eureka.client.register-with-eureka=false
  eureka.client.fetch-registry=false
  eureka.client.service-url.defaultZone=http://${eureka.instance.hostname}:${server.port}/eureka/
  ```

---

## 2. Cấu hình Eureka Client cho 3 Service (Customer, Product, Order)

### Trong file `build.gradle` của từng Service:
```groovy
ext {
    set('springCloudVersion', "2025.1.1")
}

dependencies {
    // Eureka Client Dependency
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    // ... các dependencies khác
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}
```

### Trong file class Application của từng Service:
Thêm annotation `@EnableDiscoveryClient`:
- `CustomerServiceApplication.java`
- `ProductServiceApplication.java`
- `OrderServiceApplication.java`

### Trong file `application.properties` của từng Service:
```properties
# Khai báo địa chỉ của Eureka Server để đăng ký
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
# Hiển thị IP của Service trên Dashboard thay vì hostname
eureka.instance.prefer-ip-address=true
```

---

## 3. Thứ tự khởi động và kiểm tra kết quả

1. **Khởi động Discovery Server (Eureka) trước:**
   ```bash
   ./gradlew :discovery-server:bootRun
   ```
2. **Khởi động 3 Service trong các Terminal riêng:**
   ```bash
   ./gradlew :customer-service:bootRun
   ./gradlew :product-service:bootRun
   ./gradlew :order-service:bootRun
   ```
3. **Kiểm tra trên Eureka Dashboard:**
   Mở trình duyệt truy cập: **`http://localhost:8761`**
   Phần **"Instances currently registered with Eureka"** sẽ hiển thị cả 3 service ở trạng thái **UP**:
   * `CUSTOMER-SERVICE` (Port 8081)
   * `PRODUCT-SERVICE` (Port 8082)
   * `ORDER-SERVICE` (Port 8083)
