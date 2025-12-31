# Vault Web

**Vault Web** is the core project of the **Vault Web ecosystem**.  
It is a full-stack application combining a **Spring Boot backend**, an **Angular frontend**, and a **PostgreSQL** database.

Vault Web acts as a **central dashboard** for a modular, self-hosted home server ecosystem. It provides a single entry point where multiple services are integrated into one secure web interface.

---

## What Vault Web Provides

Vault Web is responsible for:

- 💬 **Internal chats and collaboration tools**
- 🧑‍💻 **User and session management**
- 🔐 **Central authentication (JWT-based)**
- 🧩 **Frontend integration of external services**

Additional services are **not implemented directly in this repository**, but are embedded into the Vault Web frontend.

For example, file storage and file management are provided by the **Cloud Page** service:  
👉 https://github.com/Vault-Web/cloud-page

---

## Project Structure

- 📁 [**DIRECTORY.md**](https://github.com/Vault-Web/vault-web/blob/main/DIRECTORY.md) – generated project structure overview  
- 📚 [**Javadoc**](https://vault-web.github.io/vault-web) – backend API documentation  

---

## Local Development
For local development and contributions, it is recommended to fork the repository first.
Vault Web uses **Docker** for local development.

### Requirements

- Docker & Docker Compose  
- Java 21+ (Java 24 supported)  
- Node.js & npm  

---
If you plan to contribute, clone your fork instead of the main repository.

## 1. Clone the Repository

```bash
git clone https://github.com/Vault-Web/vault-web.git
cd vault-web
```

---

## 2. Environment Configuration (`.env`)

⚠️ **You do NOT need to create a `.env` file manually.**
A `.env` file already exists in the repository.

You may adjust the values if needed, but make sure that:

> **The database configuration in `.env` matches exactly with the backend `application.properties`.**

---

## 3. Start PostgreSQL and pgAdmin

```bash
docker compose up -d
```

* PostgreSQL: `localhost:<DB_PORT>`
* pgAdmin: [http://localhost:8081](http://localhost:8081)

---

## 4. Backend (Spring Boot)

Configuration file:
`backend/src/main/resources/application.properties`

Ensure the database values match the `.env` file.

Start the backend:

```bash
cd backend
./mvnw spring-boot:run
```

* API: [http://localhost:8080](http://localhost:8080)
* Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Timezone configuration (important for some environments)

On some systems, the backend may fail to start because PostgreSQL rejects a deprecated JVM timezone identifier during startup.

If you encounter an error like:

```text
FATAL: invalid value for parameter "TimeZone"
```
see [common_problems.md](./common_problems.md) for platform-specific troubleshooting and startup guidance.

---

## 5. Frontend (Angular)

```bash
cd frontend
npm install
ng serve
```

Open:
👉 [http://localhost:4200](http://localhost:4200)

---

## Notes

This project is intended for **self-hosted and home-server environments**.
Contributions and feedback are welcome.

---

## Contributing

To contribute to this project, please fork the repository and create a feature branch in your fork.
Pull requests should be opened from your forked repository to the main branch of this repository.

If you are new to the project, feel free to open an issue before starting work to discuss your idea.

## Troubleshooting

If you encounter setup or startup issues that are not covered above,
see [common_problems.md](./common_problems.md) for platform-specific guidance and fixes.