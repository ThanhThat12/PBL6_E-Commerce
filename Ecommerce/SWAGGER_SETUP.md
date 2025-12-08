# Swagger/OpenAPI Documentation Setup

## ✅ Cài đặt hoàn tất

Swagger đã được cài đặt thành công cho PBL6 E-Commerce Backend.

## 🚀 Truy cập Swagger UI

### 1. Khởi động Backend
```bash
cd d:\Proj_Nam4\PBL6_E-Commerce\Ecommerce
mvnw spring-boot:run
```

### 2. Mở trình duyệt và truy cập:

**Swagger UI (Interactive API Documentation):**
```
https://localhost:8081/swagger-ui/index.html
```

**OpenAPI JSON:**
```
https://localhost:8081/v3/api-docs
```

> ⚠️ Lưu ý: Backend đang chạy HTTPS trên port 8081

## 🔐 Cách sử dụng Authentication

### Bước 1: Đăng nhập để lấy JWT Token
1. Mở Swagger UI
2. Tìm endpoint `POST /api/auth/login`
3. Click "Try it out"
4. Nhập thông tin đăng nhập:
```json
{
  "username": "your_username",
  "password": "your_password"
}
```
5. Click "Execute"
6. Copy `access_token` từ response

### Bước 2: Authorize trong Swagger
1. Click nút **"Authorize"** (🔓 icon) ở góc trên bên phải
2. Nhập token theo format:
```
Bearer your_access_token_here
```
3. Click "Authorize"
4. Click "Close"

### Bước 3: Test các API được bảo vệ
Bây giờ bạn có thể test các API yêu cầu authentication như:
- `GET /api/seller/shop`
- `PUT /api/seller/shop`
- `POST /api/products`
- v.v.

## 📚 Các thay đổi đã thực hiện

### 1. **pom.xml** - Thêm dependency
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 2. **SwaggerConfig.java** - Configuration class
- Định nghĩa metadata cho API (title, version, description)
- Cấu hình JWT Bearer authentication
- Thêm server URLs (local + production)

### 3. **SecurityConfig.java** - Cho phép truy cập Swagger
Thêm các endpoint sau vào whitelist:
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/swagger-resources/**`
- `/webjars/**`

### 4. **application.properties** - Swagger settings
```properties
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.try-it-out-enabled=true
```

### 5. **ShopController.java** - Demo annotations
Đã thêm Swagger annotations cho endpoint `GET /api/seller/shop`:
- `@Tag` - Nhóm controller
- `@Operation` - Mô tả endpoint
- `@ApiResponses` - Mô tả các response codes
- `@SecurityRequirement` - Yêu cầu authentication

## 🎯 Thêm Swagger cho các Controller khác

### Template cơ bản:

```java
@Tag(name = "Category Name", description = "Description of this API group")
@RestController
@RequestMapping("/api/endpoint")
public class YourController {

    @Operation(
        summary = "Short summary",
        description = "Detailed description of what this endpoint does",
        security = @SecurityRequirement(name = "bearerAuth") // Nếu cần auth
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/path")
    public ResponseEntity<?> yourMethod(
        @Parameter(description = "Parameter description") @PathVariable Long id
    ) {
        // Your code
    }
}
```

## 📝 Các annotation hữu ích

### Controller level:
- `@Tag(name, description)` - Nhóm các endpoint

### Method level:
- `@Operation(summary, description)` - Mô tả endpoint
- `@ApiResponses` - Danh sách response codes
- `@SecurityRequirement` - Yêu cầu authentication

### Parameter level:
- `@Parameter(description)` - Mô tả tham số
- `@RequestBody(description)` - Mô tả request body
- `@Schema` - Định nghĩa schema cho DTO

## 🔧 Troubleshooting

### Swagger UI không load được:
1. Kiểm tra backend đã start: `https://localhost:8081/actuator/health`
2. Kiểm tra SecurityConfig đã cho phép `/swagger-ui/**`
3. Clear browser cache và thử lại

### Authentication không hoạt động:
1. Đảm bảo token format: `Bearer <token>`
2. Token phải còn hiệu lực (không expired)
3. Check console log để xem lỗi chi tiết

### CORS issues:
Đã được cấu hình sẵn trong SecurityConfig, không cần thay đổi.

## 📖 Tài liệu tham khảo

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Guide](https://swagger.io/tools/swagger-ui/)

## 🎉 Kết quả

Swagger UI giờ đây cung cấp:
- ✅ Interactive API documentation
- ✅ Try-it-out feature cho tất cả endpoints
- ✅ JWT Bearer authentication support
- ✅ Request/Response examples
- ✅ Schema definitions
- ✅ Tự động generate từ code annotations

Bạn có thể chia sẻ URL Swagger cho team frontend để họ xem API documentation và test endpoints dễ dàng hơn!
