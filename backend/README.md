# second-trade-backend

## 环境要求
- JDK 1.8
- Maven 3.x
- MySQL 8.x

## 配置说明
- 后端端口：`8000`
- MySQL：默认读取 [application.yml](file:///Users/libra520/JavaProject/biyesheji/er_shou_wu_pin/second_trade/backend/src/main/resources/application.yml) 中的配置
  - 默认库：`second_trade`（带 `createDatabaseIfNotExist=true`）
  - 默认账号：`root`
  - 默认密码：`12345678`

## 启动方式
```bash
cd backend
mvn -DskipTests spring-boot:run
```
## docker启动方式
- 根目录运行： docker compose up --build -d

## 初始化
### 方式 A：使用 SQL 脚本初始化库表
- 脚本位置：[sql/init.sql](file:///Users/libra520/JavaProject/biyesheji/er_shou_wu_pin/second_trade/backend/sql/init.sql)
- 执行后会创建库与表结构（不插入用户密码数据）。

### 方式 B：启动后自动建表/种子数据（开发）
- 当前默认 `spring.jpa.hibernate.ddl-auto=update` 会自动建表
- 当前默认 `app.seed=true`，首次启动会插入基础类目/品牌/型号与测试账号
  - 管理员：`admin / admin123`
  - 普通用户：`mockuser / 123456`

## 前端对接说明
- 前端默认请求后端：`http://127.0.0.1:8000`
- 鉴权方式：`Authorization: Bearer <access_token>`
- 登录接口返回：`access` + `refresh` + `user`

## 接口概览（与前端一致）
### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/auth/refresh`
- `GET /api/auth/me`（兼容 `/me/`）
- `PUT /api/auth/me`（兼容 `/me/`）
- `GET /api/auth/admin/users/`
- `POST /api/auth/admin/users/{id}/disable/`

### Market（基础数据）
- `GET /api/market/categories/`
- `GET /api/market/categories/{id}/`
- `GET /api/market/brands/?category_id=`
- `GET /api/market/device-models/?category_id=&brand_id=`
- `GET /api/market/device-models/{id}/`
- `GET /api/market/device-models/reference/?category_id=&device_model_id=`

### Drafts（上架草稿）
- `POST /api/market/drafts/init/`
- `POST /api/market/drafts/{draftKey}/images/`（multipart，字段名 `image`）
- `POST /api/market/drafts/{draftKey}/estimate/`
- `POST /api/market/drafts/{draftKey}/publish/`
- `POST /api/market/drafts/{draftKey}/analyze/`（占位，兼容前端封装）

### Products（商品与留言）
- `GET /api/market/products/`（市场大厅/卖家中心：通过是否传 `seller_id` 区分）
- `GET /api/market/products/{id}/`
- `GET /api/market/products/{id}/comments/`
- `POST /api/market/products/{id}/comments/`

### Orders（订单）
- `POST /api/market/orders/create_trade/`
- `POST /api/market/orders/{id}/pay/`
- `POST /api/market/orders/{id}/cancel_payment/`
- `POST /api/market/orders/{id}/ship/`
- `POST /api/market/orders/{id}/confirm_receipt/`
- `POST /api/market/orders/{id}/refund/`
- `GET /api/market/orders/buy/`
- `GET /api/market/orders/sell/`
- `GET /api/market/orders/{id}/`

### Admin（审核与订单查询）
- `GET /api/market/admin/products/pending_review/`
- `POST /api/market/admin/products/{id}/approve/`
- `POST /api/market/admin/products/{id}/reject/`
- `GET /api/market/admin/orders/`

