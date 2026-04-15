# 🎬 CineBook Pro — Movie Ticket Booking System

A full-stack **Spring Boot + MySQL** online movie ticket booking system with JWT auth, seat locking, concurrency handling, and a complete REST API.

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT |
| Database | MySQL 8 |
| ORM | Spring Data JPA (Hibernate) |
| Build Tool | Maven 3.8+ |
| Frontend | HTML5 + Vanilla JS (served by Spring Boot) |

---

## 📋 Prerequisites

Install these before running the project:

| Tool | Version | Download |
|---|---|---|
| Java JDK | 17 or higher | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org/download.cgi |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/installer/ |

> **Windows tip:** Use the MySQL Installer (the full package) — it installs MySQL Server, sets up the service, and lets you set a root password all in one go.

---

## 🚀 Setup & Run (Windows)

### Step 1 — Install Java JDK

1. Go to https://adoptium.net and download the **JDK 17** (or 21) Windows `.msi` installer
2. Run the installer — make sure to tick **"Set JAVA_HOME variable"** during setup
3. Verify in a new Command Prompt:
   ```cmd
   java -version
   ```
   You should see something like `openjdk version "17.x.x"`

---

### Step 2 — Install Maven

1. Download the **Binary zip archive** from https://maven.apache.org/download.cgi
2. Extract it somewhere like `C:\Program Files\Maven\`
3. Add Maven's `bin` folder to your system PATH:
   - Search **"Environment Variables"** in the Start menu
   - Under **System Variables**, find `Path` → click **Edit** → **New**
   - Add the path to Maven's `bin` folder, e.g. `C:\Program Files\Maven\apache-maven-3.9.x\bin`
4. Verify in a new Command Prompt:
   ```cmd
   mvn -version
   ```

---

### Step 3 — Install & Start MySQL

1. Download **MySQL Installer** from https://dev.mysql.com/downloads/installer/
2. Run the installer, choose **"Developer Default"** setup type
3. During configuration, set a **root password** — remember it, you'll need it in Step 5
4. MySQL runs as a Windows service and starts automatically after install
5. To start/stop it manually, open Command Prompt **as Administrator**:
   ```cmd
   net start mysql80
   net stop mysql80
   ```
   *(the service name might be `mysql` or `mysql80` depending on your version)*

---

### Step 4 — Create the Database

Open **MySQL Command Line Client** (installed with MySQL) or Command Prompt:

```cmd
mysql -u root -p
```

Enter your root password, then run:

```sql
CREATE DATABASE cinebook_db;
EXIT;
```

> The app auto-creates all tables on first startup — you only need to create the empty database.

---

### Step 5 — Configure Your Database Password

Open the file:
```
cinebook\src\main\resources\application.properties
```

Find these two lines and update the password to match what you set during MySQL install:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD_HERE
```

Save the file.

---

### Step 6 — Build & Run

Open **Command Prompt** in the `cinebook` project folder (the folder containing `pom.xml`).

> **Tip:** In File Explorer, navigate into the `cinebook` folder, then type `cmd` in the address bar and press Enter — it opens a Command Prompt right there.

Run:

```cmd
mvn spring-boot:run
```

The first run downloads dependencies (~50 MB) and may take a minute. You'll see Spring Boot startup logs. When you see:

```
Started CineBookApplication in X.XXX seconds
```

the app is ready.

---

### Step 7 — Open in Browser

```
http://localhost:8080
```

**Pre-loaded login credentials:**

| Role | Email | Password |
|---|---|---|
| Regular User | user@cinebook.com | user123 |
| Admin | admin@cinebook.com | admin123 |

---

## 🔁 Stopping the App

Press `Ctrl + C` in the Command Prompt window where the app is running.

---

## 🛠️ Running in VS Code (Optional)

If you prefer an IDE:

1. Install **VS Code** from https://code.visualstudio.com
2. Install these two VS Code extensions:
   - **Extension Pack for Java** (by Microsoft)
   - **Spring Boot Extension Pack** (by VMware)
3. Open the `cinebook` folder in VS Code (`File → Open Folder`)
4. VS Code auto-detects the Spring Boot project
5. Click the **Run** button that appears above the `main` method in `CineBookApplication.java`, or press `F5`
6. Open `http://localhost:8080`

---

## 🐛 Common Issues on Windows

| Problem | Fix |
|---|---|
| `'java' is not recognized` | JDK not installed or `JAVA_HOME` not set — reinstall JDK and tick "Set JAVA_HOME" |
| `'mvn' is not recognized` | Maven `bin` folder not added to PATH — see Step 2 |
| `Access denied for user 'root'` | Wrong password in `application.properties` — update it to match your MySQL root password |
| `Unknown database 'cinebook_db'` | Database not created — run `CREATE DATABASE cinebook_db;` in MySQL (Step 4) |
| `Port 8080 already in use` | Another app is using port 8080 — change `server.port=8081` in `application.properties` and open `http://localhost:8081` |
| MySQL service not running | Run `net start mysql80` in an Administrator Command Prompt |
| First run is slow | Normal — Maven downloads ~50 MB of dependencies once and caches them |

---

## 💳 Promo Codes

| Code | Discount |
|---|---|
| `SAVE10` | 10% off |
| `FIRST50` | 50% off |
| `WEEKEND20` | 20% off |

---

## 🔌 API Endpoints (for reference)

| Method | URL | Auth |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/movies` | Public |
| GET | `/api/showtimes/movie/{id}` | Public |
| GET | `/api/seats/showtime/{id}` | User |
| POST | `/api/seats/lock` | User |
| POST | `/api/bookings` | User |
| GET | `/api/bookings` | User |
| DELETE | `/api/bookings/{id}/cancel` | User |
| GET | `/api/bookings/analytics` | Admin |
