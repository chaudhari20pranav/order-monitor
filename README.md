# 🛍️ Order Monitor – Real-Time E-Commerce Order Monitoring Platform

A production-ready, real-time e-commerce order monitoring and management platform built with **Java 21 + Spring Boot**, featuring live WebSocket updates, role-based dashboards, HTML email notifications, and deployment on **Render** with **NeonDB** PostgreSQL.

---

## 🌟 Project Overview

Order Monitor is an e-commerce operations dashboard that demonstrates modern backend engineering patterns:

- **Real-time** order tracking via WebSockets (STOMP over SockJS)
- **Event-driven** architecture using Spring Application Events
- **Role-based** system: Admins manage all orders; Subscribers manage their own
- **Business timeline logic**: 24h ship window, 48h delivery window with countdown alerts
- **Admin inactivity reminder** via scheduled jobs + email
- **HTML email notifications** via Gmail SMTP

---

## ✨ Features

### Subscriber
| Feature | Description |
|---|---|
| Register / Login | Session-based authentication with BCrypt |
| Place Orders | Order any product by name, category, quantity, and price |
| Pay for Orders | Mark orders as paid |
| Cancel Orders | Cancel active orders |
| Live Order Cards | Real-time status via WebSocket |
| Countdown Timers | Shows time remaining until shipping/delivery deadlines |
| In-App Notifications | Per-event notification panel |
| Email Notifications | HTML emails for placed, shipped, delivered, cancelled |

### Admin
| Feature | Description |
|---|---|
| Operations Dashboard | Stats: total, delayed shipments, delayed deliveries, active subscribers |
| Live Order Table | All orders with search, status badges, countdowns |
| Status Updates | Update any order status via modal |
| Live Activity Feed | Real-time audit log via WebSocket |
| Notification Bell | Live alert panel |
| Inactivity Reminder | Email + dashboard alert if inactive for 8+ hours |

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Web | Spring Web MVC |
| Real-time | Spring WebSocket + STOMP + SockJS |
| Database | PostgreSQL via NeonDB |
| ORM | Spring Data JPA + Hibernate |
| Email | Spring Mail + Gmail SMTP |
| Templates | Thymeleaf |
| Frontend | Bootstrap 5, Vanilla JS |
| Security | BCrypt (spring-security-crypto) |
| Build | Maven |
| Container | Docker (multi-stage) |
| Deployment | Render |

---

## 🏗 Architecture Flow

```
HTTP Request
     │
     ▼
Controller
     │  validates input / checks session
     ▼
Service (OrderService / UserService)
     │  business logic
     ▼
Repository (JPA) ──► PostgreSQL / NeonDB
     │
     ▼
ApplicationEventPublisher.publish(event)
     │
     ▼
OrderEventListener
     ├── AuditService.log()           → order_events table
     ├── NotificationService.create() → notifications table
     ├── WebSocketService.broadcast() → /topic/orders + /topic/notifications
     └── EmailService.send()          → Gmail SMTP (async thread)
```

---

## 🔌 WebSocket Flow

```
Browser                        Server
   │                              │
   │── SockJS connect ──────────► │ /ws-orders
   │◄── CONNECTED ────────────────│
   │                              │
   │── SUBSCRIBE ────────────────►│ /topic/orders
   │── SUBSCRIBE ────────────────►│ /topic/notifications
   │                              │
   │          [Subscriber places order]
   │                              │
   │◄── ORDER_PLACED ─────────────│  WsNotification { type, message, order, targetUserId }
   │  (updates order card UI)     │
   │                              │
   │          [Admin marks SHIPPED]
   │                              │
   │◄── ORDER_SHIPPED ────────────│  WsNotification
   │  (updates table row)         │
```

---

## 📧 Email Notification Flow

```
OrderEventListener
     │
     ├── onOrderPlaced()    → emailService.sendOrderPlacedEmail(user, order)
     ├── onOrderShipped()   → emailService.sendOrderShippedEmail(user, order)
     ├── onOrderDelivered() → emailService.sendOrderDeliveredEmail(user, order)
     ├── onOrderCancelled() → emailService.sendOrderCancelledEmail(user, order)
     └── onReminderGenerated() → emailService.sendAdminReminderEmail(admin)

All emails sent from: online.monitor.apt@gmail.com
All @Async – never block the HTTP thread
```

---

## 👥 Role-Based System

```
/register → choose ADMIN or SUBSCRIBER
/login    → session stores { userId, userRole, userName }

GET /           → redirects based on role
GET /admin/dashboard      → ADMIN only
GET /subscriber/dashboard → SUBSCRIBER only

POST /admin/orders/{id}/status  → ADMIN only
POST /subscriber/orders         → SUBSCRIBER only (place)
POST /subscriber/orders/{id}/pay    → SUBSCRIBER only
POST /subscriber/orders/{id}/cancel → SUBSCRIBER only
```

---

## ⏱ Order Timeline Logic

```
Order Placed
    │
    ▼
Must be SHIPPED within 24 hours
    │  ← Admin sees countdown: "Ship in HH:MM:SS"
    │  ← If overdue: row highlighted red, "OVERDUE" shown
    ▼
Must be DELIVERED within 48 hours of shipping
    │  ← Admin sees countdown: "Deliver in HH:MM:SS"
    ▼
DELIVERED ✅
```

Delayed orders appear in:
- `stats.delayedShipments` counter
- `stats.delayedDeliveries` counter
- Highlighted in the order table with urgent countdown timer

---

## 🔔 Admin Reminder System

The `AdminReminderScheduler` runs **every hour**.

If an admin's `last_active` timestamp is older than **8 hours**:

1. A `ReminderGeneratedEvent` is published
2. `OrderEventListener.onReminderGenerated()`:
   - Broadcasts a WebSocket alert → all connected browser sessions
   - Sends an HTML reminder email to that admin

`last_active` is refreshed every time the admin loads their dashboard.

---

## 🗄 NeonDB Setup

1. Go to [neon.tech](https://neon.tech) and create a free project
2. Copy your connection string (it looks like `postgresql://user:pass@host/dbname?sslmode=require`)
3. Open the SQL editor in NeonDB and run:

```sql
-- Paste contents of sql/schema.sql here
```

4. Set your environment variables (see below)

---

## 🚀 Render Deployment Steps

### Prerequisites
- GitHub account with the project pushed
- NeonDB connection string
- Gmail App Password (see Gmail Setup below)

### Steps

1. **Log in to [render.com](https://render.com)**
2. Click **New → Web Service**
3. Connect your GitHub repository
4. Configure:
   - **Environment**: Docker
   - **Dockerfile path**: `./Dockerfile`
   - **Instance Type**: Free (or Starter for better performance)
5. Add all **Environment Variables** (see table below)
6. Click **Deploy**

### Gmail App Password Setup
1. Enable 2FA on your Google account
2. Go to **Google Account → Security → App Passwords**
3. Create a new App Password for "Mail"
4. Use that 16-character password as `MAIL_PASSWORD`

---

## 🔧 Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | NeonDB JDBC URL | `jdbc:postgresql://host/db?sslmode=require` |
| `DB_USERNAME` | NeonDB username | `alice` |
| `DB_PASSWORD` | NeonDB password | `superSecret` |
| `MAIL_USERNAME` | Gmail address | `online.monitor.apt@gmail.com` |
| `MAIL_PASSWORD` | Gmail App Password | `abcd efgh ijkl mnop` |
| `PORT` | Server port (Render sets this automatically) | `8080` |

---

## 🐳 Docker Usage

### Build locally
```bash
docker build -t order-monitor .
```

### Run locally
```bash
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host/db?sslmode=require" \
  -e DB_USERNAME="user" \
  -e DB_PASSWORD="pass" \
  -e MAIL_USERNAME="online.monitor.apt@gmail.com" \
  -e MAIL_PASSWORD="your-app-password" \
  order-monitor
```

### Multi-stage build details
- **Stage 1** (`maven:3.9.6-eclipse-temurin-21`): Downloads dependencies, compiles, packages JAR
- **Stage 2** (`eclipse-temurin:21-jre-alpine`): Minimal JRE runtime, non-root user, optimised JVM flags

---

## 🗂 Project Structure

```
order-monitor/
├── Dockerfile
├── pom.xml
├── README.md
├── sql/
│   └── schema.sql
└── src/
    ├── main/
    │   ├── java/com/ordermonitor/
    │   │   ├── OrderMonitorApplication.java
    │   │   ├── config/
    │   │   │   ├── AsyncConfig.java
    │   │   │   ├── PasswordConfig.java
    │   │   │   └── WebSocketConfig.java
    │   │   ├── controller/
    │   │   │   ├── AdminController.java
    │   │   │   ├── AuthController.java
    │   │   │   └── SubscriberController.java
    │   │   ├── dto/
    │   │   │   ├── AdminDashboardStats.java
    │   │   │   ├── LoginRequest.java
    │   │   │   ├── OrderRequest.java
    │   │   │   ├── OrderResponse.java
    │   │   │   ├── RegisterRequest.java
    │   │   │   └── WsNotification.java
    │   │   ├── entity/
    │   │   │   ├── Notification.java
    │   │   │   ├── Order.java
    │   │   │   ├── OrderEvent.java
    │   │   │   └── User.java
    │   │   ├── event/
    │   │   │   ├── OrderCancelledEvent.java
    │   │   │   ├── OrderDeliveredEvent.java
    │   │   │   ├── OrderPlacedEvent.java
    │   │   │   ├── OrderShippedEvent.java
    │   │   │   ├── PaymentCompletedEvent.java
    │   │   │   └── ReminderGeneratedEvent.java
    │   │   ├── listener/
    │   │   │   └── OrderEventListener.java
    │   │   ├── repository/
    │   │   │   ├── NotificationRepository.java
    │   │   │   ├── OrderEventRepository.java
    │   │   │   ├── OrderRepository.java
    │   │   │   └── UserRepository.java
    │   │   ├── scheduler/
    │   │   │   └── AdminReminderScheduler.java
    │   │   ├── service/
    │   │   │   ├── AuditService.java
    │   │   │   ├── EmailService.java
    │   │   │   ├── NotificationService.java
    │   │   │   ├── OrderService.java
    │   │   │   └── UserService.java
    │   │   ├── util/
    │   │   │   └── GlobalExceptionHandler.java
    │   │   └── websocket/
    │   │       └── OrderWebSocketService.java
    │   └── resources/
    │       ├── application.properties
    │       ├── static/
    │       │   ├── css/
    │       │   │   ├── auth.css
    │       │   │   └── dashboard.css
    │       │   └── js/
    │       │       ├── admin.js
    │       │       └── subscriber.js
    │       └── templates/
    │           ├── auth/
    │           │   ├── login.html
    │           │   └── register.html
    │           ├── admin/
    │           │   └── dashboard.html
    │           └── subscriber/
    │               └── dashboard.html
    └── test/
        ├── java/com/ordermonitor/
        │   ├── repository/
        │   │   └── OrderRepositoryTest.java
        │   └── service/
        │       ├── OrderServiceTest.java
        │       └── UserServiceTest.java
        └── resources/
            └── application.properties
```

---

## 🖼 Screenshots Section

> Replace with actual screenshots after deployment.

| Page | Description |
|---|---|
| `/login` | Dark e-commerce login with promo panel |
| `/register` | Role selector (Admin / Subscriber) |
| `/admin/dashboard` | Live order table, stat cards, activity feed |
| `/subscriber/dashboard` | Order cards, place order form, notifications |

---

## 🔮 Future Improvements

- [ ] Chart.js analytics panel (volume by stock, status distribution)
- [ ] CSV/PDF order export
- [ ] Order search and filtering by date range / product name
- [ ] Two-factor authentication
- [ ] WebSocket heartbeat indicator
- [ ] Dark/Light theme toggle
- [ ] Pagination for large order sets
- [ ] Order history timeline view per order
- [ ] Admin bulk status update

---

## 📄 License

MIT License – free to use and modify.

---

## 🙋 Author

Built as a real-world e-commerce engineering showcase.  
Questions or contributions? Open a GitHub issue!
