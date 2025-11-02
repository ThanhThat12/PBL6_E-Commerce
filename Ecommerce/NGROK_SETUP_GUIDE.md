# Hướng Dẫn Setup Ngrok Cho Backend (BE)

Ngrok là một công cụ giúp bạn tạo một đường dẫn public (tạm thời) cho server backend đang chạy trên máy local, rất hữu ích khi cần test webhook, tích hợp với các dịch vụ bên ngoài hoặc demo nhanh.

## Bước 1: Đăng ký và tải về Ngrok

1. Truy cập [https://ngrok.com/](https://ngrok.com/) và đăng ký tài khoản (miễn phí).
2. Sau khi đăng ký, vào trang Dashboard để lấy **Auth Token**.
3. Tải về Ngrok phù hợp với hệ điều hành của bạn (Windows/macOS/Linux).

## Bước 2: Cài đặt Ngrok

- Giải nén file tải về.
- Đặt file `ngrok.exe` vào thư mục mong muốn (có thể thêm vào PATH để tiện sử dụng).

## Bước 3: Kết nối Ngrok với tài khoản

Mở terminal/cmd và chạy lệnh sau (thay `YOUR_AUTHTOKEN` bằng token của bạn):

```sh
ngrok config add-authtoken YOUR_AUTHTOKEN
```

## Bước 4: Chạy Ngrok cho Backend

Giả sử backend của bạn chạy ở cổng 8080, dùng lệnh:

```sh
ngrok http 8080
```

- Sau khi chạy, Ngrok sẽ cung cấp một URL public (dạng `https://xxxx.ngrok.io`).
- Dùng URL này để test webhook hoặc chia sẻ cho bên ngoài truy cập vào backend local của bạn.

## Một số lưu ý

- Nếu backend chạy ở cổng khác, thay `8080` bằng cổng tương ứng.
- Để dừng Ngrok, nhấn `Ctrl + C` trong terminal đang chạy.
- Có thể tham khảo thêm các tuỳ chọn khác bằng lệnh:

```sh
ngrok help
```

---

**Tài liệu tham khảo:**

- [Ngrok Documentation](https://ngrok.com/docs)
- [Ngrok Getting Started](https://ngrok.com/docs/getting-started/)
