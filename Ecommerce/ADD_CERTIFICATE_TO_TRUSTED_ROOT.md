# Hướng dẫn thêm Self-Signed Certificate vào Trusted Root (Tùy chọn)

## Cách 1: Accept certificate mỗi lần (Đơn giản - Khuyên dùng)

Mở https://localhost:8081 trong trình duyệt và click "Advanced" → "Proceed to localhost"

## Cách 2: Import vào Windows Trusted Root (Nâng cao)

### Bước 1: Export certificate từ keystore

```powershell
cd D:\PBL6\PBL6_E-Commerce\Ecommerce\src\main\resources

keytool -exportcert -alias ecommerce -keystore keystore.p12 -storetype PKCS12 -storepass password -file ecommerce.cer
```

### Bước 2: Import vào Windows Trusted Root

1. **Mở Certificate Manager:**
   - Nhấn `Win + R`
   - Gõ `certmgr.msc`
   - Nhấn Enter

2. **Import Certificate:**
   - Trong cửa sổ Certificate Manager
   - Mở rộng **"Trusted Root Certification Authorities"**
   - Click chuột phải vào **"Certificates"**
   - Chọn **"All Tasks"** → **"Import..."**
   - Browse đến file `ecommerce.cer` vừa export
   - Nhấn **Next** → **Finish**

3. **Restart trình duyệt**

### Bước 3: Xóa certificate khi không dùng nữa

Khi không cần certificate này nữa (chuyển sang production):

1. Mở `certmgr.msc`
2. Đi đến **"Trusted Root Certification Authorities"** → **"Certificates"**
3. Tìm certificate **"localhost"** hoặc **"ecommerce"**
4. Click chuột phải → **"Delete"**

## Lưu ý

- Chỉ nên import self-signed certificate vào Trusted Root trong môi trường **development**
- Trong production, sử dụng certificate hợp lệ từ Let's Encrypt hoặc CA khác
- Certificate này chỉ hợp lệ cho **localhost**, không dùng được cho domain khác
