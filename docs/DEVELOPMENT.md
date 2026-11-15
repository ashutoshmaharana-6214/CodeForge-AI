# Local development

Prerequisites: Java 21, Maven, Node.js 22+, npm.

Backend:
```bash
cd backend
mvn spring-boot:run
```

Frontend (new terminal):
```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Backend API: http://localhost:8080/api/problems.

**Current limitations:** Problem catalog is seeded in memory; code execution, authenticated submissions, AI tool calling, PostgreSQL, Kafka, Docker, Kubernetes, and verified progress are not implemented in this commit. Never commit secrets or hidden judging tests.
