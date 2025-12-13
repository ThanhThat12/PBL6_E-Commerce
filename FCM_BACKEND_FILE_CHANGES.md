# Backend FCM Integration - File Changes Summary

## 1. File thêm mới

- `src/main/java/com/PBL6/Ecommerce/domain/UserFCMToken.java`  x
  Entity lưu FCM token của user.
- `src/main/java/com/PBL6/Ecommerce/repository/UserFCMTokenRepository.java`  x
  Repository thao tác với bảng token.
- `src/main/java/com/PBL6/Ecommerce/dto/UserFCMTokenDTO.java`  
  DTO nhận dữ liệu FCM token từ client.
- `src/main/java/com/PBL6/Ecommerce/controller/UserFCMTokenController.java`  
  API nhận/lưu/xóa FCM token từ mobile app.

## 2. File thay đổi

- `src/main/java/com/PBL6/Ecommerce/service/NotificationService.java`  
  Thêm logic gửi notification qua FCM, lấy token từ repository.
- `src/main/resources/application.properties`  
  (Có thể thêm cấu hình liên quan Firebase nếu cần)
- `.gitignore`  
  Thêm dòng: `Ecommerce/src/main/resources/firebase-service-account.json`

## 3. File không được push lên GitHub

- `src/main/resources/firebase-service-account.json`  
  (Chỉ dùng cho backend, không commit lên repo)

---

**Lưu ý:**
- Luôn thêm file bí mật vào `.gitignore`.
- Nếu lỡ commit, phải xóa khỏi toàn bộ lịch sử git trước khi push.
- Không bao giờ đẩy file bí mật lên GitHub công khai.
