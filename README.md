# LabBooking

Ứng dụng Android đặt gói dịch vụ phòng lab: người dùng xem các gói, thêm vào giỏ, chọn cơ sở và thời gian, đặt đơn và nhắn tin với quản trị viên.

Đây là dự án nhóm 5 người, thực hiện từ 16/06/2025 đến 13/07/2025.

## Chức năng

- Đăng ký, đăng nhập bằng Firebase Authentication
- Trang chủ với slider ảnh và danh sách gói dịch vụ
- Chi tiết gói, giỏ hàng, xác nhận giỏ hàng và xác nhận đơn
- Lịch sử đơn hàng, sắp xếp từ mới đến cũ
- Chat giữa người dùng và quản trị viên; quản trị viên có danh sách phòng chat riêng
- Bản đồ vị trí phòng lab (Google Maps)
- Thông báo nhắc giỏ hàng

## Công nghệ

| Thành phần | Sử dụng |
|---|---|
| Ngôn ngữ | Java |
| Nền tảng | Android, minSdk 26, targetSdk 35 |
| Xác thực | Firebase Authentication |
| Dữ liệu | Cloud Firestore, FirebaseUI Firestore |
| Bản đồ | Google Maps SDK (play-services-maps) |
| Ảnh | Glide |
| Build | Gradle Kotlin DSL, version catalog |

## Cấu trúc mã nguồn

```
app/src/main/java/io/diendanx/labbooking/
├── activities/   # Màn hình, chia theo tính năng (Home, Cart, Order, SignIn, ...)
├── models/       # Lớp dữ liệu ánh xạ với document Firestore
├── repository/   # BaseRepository<T> generic và repository cho từng collection
├── services/     # Dịch vụ thông báo giỏ hàng
└── Helpers/      # Tiện ích ngày giờ, kiểm tra dữ liệu, Firebase
```

Firestore dùng các collection: `users`, `package`, `facility`, `cart`, `order`, `order_item`, `chat_room`, `chat_messages`.

## Cài đặt

Repo không chứa cấu hình Firebase và khóa Google Maps, nên cần tự tạo trước khi build.

1. Clone repo và mở bằng Android Studio.
2. Tạo một project Firebase, thêm ứng dụng Android với package `io.diendanx.labbooking`, bật Authentication (Email/Password) và Cloud Firestore.
3. Tải `google-services.json` từ Firebase Console và đặt vào thư mục `app/`.
4. Tạo file `secrets.properties` ở thư mục gốc với nội dung:

   ```
   MAPS_API_KEY=<khóa Google Maps của bạn>
   ```

5. Sync Gradle rồi chạy trên máy ảo hoặc thiết bị Android 8.0 trở lên.

## Thành viên

| Thành viên | GitHub |
|---|---|
| Trưởng nhóm | [dienDan10](https://github.com/dienDan10) |
| Thành viên | [minhdang0603](https://github.com/minhdang0603) |
| Thành viên | [haianh18](https://github.com/haianh18) |
| Thành viên | [2bars-la-dev](https://github.com/2bars-la-dev) |
| Thành viên | [QuanHT2412](https://github.com/QuanHT2412) |

### Phần tôi đảm nhiệm (QuanHT2412)

- Trang chủ: slider ảnh, danh sách gói dịch vụ và giao diện từng gói (`ProductsFragment`, `ProductAdapter`, `ImageSliderAdapter`)
- Màn hình Cài đặt (`SettingsFragment`)
- Lịch sử đơn hàng (`OrderListActivity`, `OrderListAdapter`)
- Icon ứng dụng

Repo gốc của nhóm do trưởng nhóm quản lý; đây là bản lưu trên tài khoản cá nhân, giữ nguyên lịch sử commit của cả nhóm.
