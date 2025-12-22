# CƠ SỞ LÝ THUYẾT - HỆ THỐNG E-COMMERCE PBL6

## 1. KIẾN TRÚC HỆ THỐNG

### 1.1. Mô hình MVC (Model-View-Controller)
- **Model**: Tầng dữ liệu, xử lý logic nghiệp vụ và tương tác với database
- **View**: Giao diện người dùng (Frontend React/Mobile React Native)
- **Controller**: Điều phối request/response giữa Model và View

### 1.2. Kiến trúc Microservices
- **API Gateway**: Điểm truy cập duy nhất cho tất cả các service
- **Service Independence**: Các module độc lập (User, Product, Order, Payment, Chat, etc.)
- **Scalability**: Khả năng mở rộng từng service riêng biệt
- **Fault Isolation**: Lỗi ở một service không ảnh hưởng toàn bộ hệ thống

### 1.3. RESTful API Design
- **HTTP Methods**: GET, POST, PUT, PATCH, DELETE
- **Stateless**: Mỗi request độc lập, không lưu trạng thái
- **Resource-based URLs**: `/api/products`, `/api/orders`, `/api/users`
- **Status Codes**: 200 (OK), 201 (Created), 400 (Bad Request), 401 (Unauthorized), 404 (Not Found), 500 (Server Error)

## 2. CÔNG NGHỆ VÀ FRAMEWORKS

### 2.1. Backend - Spring Boot
- **Spring Framework**: IoC Container, Dependency Injection
- **Spring Boot**: Auto-configuration, Embedded Server
- **Spring Security**: Authentication, Authorization, JWT
- **Spring Data JPA**: ORM, Repository Pattern
- **Hibernate**: ORM Implementation, Entity Mapping

### 2.2. Frontend - React
- **Component-based Architecture**: Tái sử dụng code
- **Virtual DOM**: Tối ưu hiệu năng rendering
- **State Management**: React Context, Redux/Zustand
- **React Router**: Client-side routing

### 2.3. Mobile - React Native
- **Cross-platform Development**: iOS và Android từ codebase duy nhất
- **Native Components**: Hiệu năng gần native app
- **Expo**: Development toolchain và SDK

### 2.4. Database - MySQL
- **Relational Database**: ACID properties
- **Normalization**: Giảm dư thừa dữ liệu
- **Indexing**: Tối ưu query performance
- **Foreign Keys**: Đảm bảo tính toàn vẹn dữ liệu

## 3. BẢO MẬT VÀ XÁC THỰC

### 3.1. JWT (JSON Web Token)
- **Stateless Authentication**: Không cần lưu session server-side
- **Token Structure**: Header.Payload.Signature
- **Token Types**:
  - Access Token: Thời gian sống ngắn (15-30 phút)
  - Refresh Token: Thời gian sống dài (7-30 ngày)

### 3.2. OAuth 2.0
- **Social Login**: Google, Facebook authentication
- **Authorization Flow**: 
  1. User redirects to OAuth provider
  2. User authorizes application
  3. Provider returns authorization code
  4. Exchange code for access token
  5. Access user information

### 3.3. HTTPS/SSL
- **Encryption**: Mã hóa dữ liệu truyền tải
- **Certificate**: Xác thực server identity
- **TLS Handshake**: Thiết lập secure connection

### 3.4. Password Security
- **Hashing**: BCrypt algorithm (one-way encryption)
- **Salt**: Random data thêm vào password trước khi hash
- **Work Factor**: Tăng độ khó để brute-force

## 4. DESIGN PATTERNS

### 4.1. Repository Pattern
- **Abstraction**: Tách logic truy cập dữ liệu
- **Testability**: Dễ dàng mock data cho testing
- **Centralization**: Tập trung query logic

### 4.2. Service Layer Pattern
- **Business Logic**: Tách logic nghiệp vụ khỏi controller
- **Reusability**: Sử dụng lại code trong nhiều controller
- **Transaction Management**: Quản lý database transactions

### 4.3. DTO (Data Transfer Object)
- **Decoupling**: Tách entity và API response/request
- **Security**: Không expose internal structure
- **Validation**: Centralized input validation

### 4.4. Builder Pattern
- **Complex Object Creation**: Tạo object với nhiều parameters
- **Fluent API**: Code dễ đọc và maintain
- **Immutability**: Object không thay đổi sau khi tạo

### 4.5. Singleton Pattern
- **Single Instance**: Chỉ một instance trong JVM
- **Global Access**: Truy cập từ mọi nơi
- **Use Cases**: Configuration, Logger, Connection Pool

## 5. CSDL VÀ QUAN HỆ DỮ LIỆU

### 5.1. Entity Relationships
- **One-to-One**: User ↔ Wallet, User ↔ Cart
- **One-to-Many**: 
  - User → Addresses
  - Shop → Products
  - Product → Reviews
  - Order → OrderItems
- **Many-to-Many**: 
  - Product ↔ Categories (ProductCategory)
  - Cart ↔ Products (CartItem)

### 5.2. Normalization
- **1NF**: Atomic values, no repeating groups
- **2NF**: No partial dependencies
- **3NF**: No transitive dependencies
- **Denormalization**: Tối ưu performance cho read operations

### 5.3. Indexing Strategy
- **Primary Key Index**: Auto-created, clustered
- **Foreign Key Index**: Tối ưu JOIN operations
- **Unique Index**: Email, username fields
- **Composite Index**: Multiple columns (shop_id, status)

## 6. TÍCH HỢP BÊN NGOÀI

### 6.1. Payment Gateway
- **VNPay Integration**: 
  - Secure Hash: HMAC SHA512
  - Return URL: Callback sau thanh toán
  - IPN URL: Server-to-server notification
- **Transaction Lifecycle**: Pending → Processing → Success/Failed

### 6.2. Shipping API (GHN - Giao Hàng Nhanh)
- **Address Validation**: Province → District → Ward
- **Shipping Fee Calculation**: Weight, dimensions, distance
- **Order Tracking**: Real-time status updates
- **Webhook**: Nhận thông báo status changes

### 6.3. Cloud Storage (Cloudinary)
- **Image Upload**: Multi-part form data
- **Transformation**: Resize, crop, format conversion
- **CDN**: Global content delivery
- **Optimization**: Auto format, quality adjustment

### 6.4. Firebase
- **Push Notifications**: Cloud Messaging (FCM)
- **Authentication**: Social login providers
- **Real-time Database**: Sync data across clients
- **Analytics**: User behavior tracking

## 7. REAL-TIME COMMUNICATION

### 7.1. WebSocket
- **Bidirectional Communication**: Server ↔ Client
- **Persistent Connection**: Không cần polling
- **Use Cases**: Chat, notifications, live updates
- **Protocol**: ws:// (unsecure), wss:// (secure)

### 7.2. STOMP Protocol
- **Message Broker**: Routing messages
- **Topic**: Broadcast to subscribers
- **Queue**: Point-to-point messaging
- **Subscription**: Client subscribe to destinations

### 7.3. SockJS
- **Fallback Mechanism**: WebSocket → HTTP streaming → Polling
- **Cross-browser Support**: Tương thích nhiều browser
- **Transparent**: Giống WebSocket API

## 8. CACHING VÀ PERFORMANCE

### 8.1. Caching Strategies
- **In-Memory Cache**: Redis, Caffeine
- **Database Query Cache**: Hibernate L2 cache
- **HTTP Cache**: ETag, Cache-Control headers
- **CDN Cache**: Static assets (images, CSS, JS)

### 8.2. Database Optimization
- **Query Optimization**: 
  - Use indexes
  - Avoid N+1 queries
  - Use JOIN instead of multiple queries
  - Pagination với LIMIT/OFFSET
- **Connection Pooling**: HikariCP
- **Lazy Loading**: Fetch data khi cần

### 8.3. API Performance
- **Pagination**: Giảm data transfer
- **Compression**: GZIP response
- **Async Processing**: @Async cho long-running tasks
- **Rate Limiting**: Prevent abuse

## 9. TESTING

### 9.1. Unit Testing
- **JUnit 5**: Testing framework
- **Mockito**: Mock dependencies
- **Test Coverage**: Minimum 70-80%
- **Assertions**: assertEquals, assertTrue, assertThrows

### 9.2. Integration Testing
- **@SpringBootTest**: Load full application context
- **TestRestTemplate**: Test REST endpoints
- **@DataJpaTest**: Test repository layer
- **H2 Database**: In-memory database cho testing

### 9.3. API Testing
- **Postman**: Manual và automated testing
- **Swagger/OpenAPI**: Interactive API documentation
- **Contract Testing**: Verify API contract

## 10. DEVOPS VÀ DEPLOYMENT

### 10.1. Docker
- **Containerization**: Package application + dependencies
- **Dockerfile**: Build instructions
- **Docker Compose**: Multi-container orchestration
- **Image Registry**: Docker Hub, private registry

### 10.2. CI/CD
- **Continuous Integration**: Auto build và test
- **Continuous Deployment**: Auto deploy to production
- **Version Control**: Git, GitHub
- **Pipeline**: Build → Test → Deploy

### 10.3. Monitoring
- **Logging**: SLF4J, Logback
- **Metrics**: Actuator endpoints
- **Error Tracking**: Sentry, Log aggregation
- **Health Checks**: /actuator/health

## 11. BUSINESS LOGIC

### 11.1. E-Commerce Flow
1. **User Registration/Login** → Verify → Create session
2. **Browse Products** → Search/Filter → View details
3. **Add to Cart** → Select variants → Update quantity
4. **Checkout** → Select address → Choose shipping
5. **Payment** → Process transaction → Confirm order
6. **Order Fulfillment** → Seller prepares → Ship → Deliver
7. **Post-Purchase** → Review product → Request refund (if needed)

### 11.2. Order States
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED → COMPLETED
                ↓                       ↓           ↓
            CANCELLED              CANCELLED   REFUNDED
```

### 11.3. Payment Flow
```
PENDING → PROCESSING → SUCCESS
              ↓
          FAILED → REFUNDED
```

### 11.4. Wallet System
- **Top-up**: Add funds via payment gateway
- **Withdraw**: Transfer to bank account
- **Transaction**: Payment, refund, commission
- **Balance Tracking**: Real-time balance updates

## 12. SECURITY BEST PRACTICES

### 12.1. Input Validation
- **Server-side Validation**: Never trust client input
- **Bean Validation**: @NotNull, @Size, @Email, @Pattern
- **SQL Injection Prevention**: Parameterized queries
- **XSS Prevention**: Escape HTML output

### 12.2. Authorization
- **Role-based Access Control (RBAC)**: ADMIN, SELLER, BUYER
- **Method Security**: @PreAuthorize, @Secured
- **Resource Ownership**: User chỉ access own data
- **Admin Privileges**: Elevated permissions

### 12.3. Data Protection
- **HTTPS Only**: Force SSL/TLS
- **Sensitive Data**: Encrypt at rest
- **Personal Data**: GDPR compliance
- **Audit Trail**: Log sensitive operations

## 13. SCALABILITY CONSIDERATIONS

### 13.1. Horizontal Scaling
- **Load Balancer**: Distribute traffic
- **Stateless Services**: No session affinity
- **Database Replication**: Master-slave setup
- **Shared Cache**: Redis cluster

### 13.2. Vertical Scaling
- **Resource Optimization**: CPU, Memory tuning
- **JVM Tuning**: Heap size, GC configuration
- **Database Optimization**: Query tuning, indexing

### 13.3. Asynchronous Processing
- **Message Queue**: RabbitMQ, Kafka
- **Background Jobs**: Email sending, report generation
- **Event-driven Architecture**: Decouple services

## 14. ERROR HANDLING

### 14.1. Exception Hierarchy
- **Global Exception Handler**: @ControllerAdvice
- **Custom Exceptions**: BusinessException, ResourceNotFoundException
- **Error Response**: Consistent format với error code, message
- **HTTP Status Codes**: Appropriate status cho mỗi error type

### 14.2. Logging Strategy
- **Log Levels**: TRACE, DEBUG, INFO, WARN, ERROR
- **Contextual Logging**: Request ID, User ID
- **Structured Logging**: JSON format
- **Log Rotation**: Prevent disk full

## 15. API DOCUMENTATION

### 15.1. OpenAPI/Swagger
- **Interactive Docs**: Try API directly
- **Schema Definition**: Request/response models
- **Authentication**: Test với real tokens
- **Auto-generation**: From code annotations

### 15.2. API Versioning
- **URI Versioning**: /api/v1/products, /api/v2/products
- **Backward Compatibility**: Support old versions
- **Deprecation**: Announce before removal

## 16. NOTIFICATION SYSTEM

### 16.1. Notification Types
- **Push Notifications**: Mobile app (FCM)
- **In-app Notifications**: WebSocket real-time
- **Email Notifications**: SMTP
- **SMS Notifications**: Third-party gateway

### 16.2. Notification Events
- Order status change
- Payment confirmation
- Shipping updates
- New messages
- Product reviews
- Promotional campaigns

## TÀI LIỆU THAM KHẢO

1. **Spring Framework Documentation**: https://spring.io/projects/spring-framework
2. **Spring Boot Reference**: https://docs.spring.io/spring-boot/docs/current/reference/html/
3. **React Documentation**: https://react.dev/
4. **React Native Documentation**: https://reactnative.dev/
5. **JWT Introduction**: https://jwt.io/introduction
6. **RESTful API Design**: https://restfulapi.net/
7. **MySQL Documentation**: https://dev.mysql.com/doc/
8. **Docker Documentation**: https://docs.docker.com/
9. **OAuth 2.0**: https://oauth.net/2/
10. **WebSocket Protocol**: https://datatracker.ietf.org/doc/html/rfc6455
