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

## Public sample runner (development only)

The **Run public sample** button calls `POST /api/runs` with `{ "problemId": 1, "sourceCode": "..." }`.
The response contains a `runId`; the browser polls `GET /api/runs/{runId}`.
Only the public example is evaluated. The result `SAMPLE_PASSED` is **not** an accepted submission
and does not count towards solved problems or streaks.

The backend **does not** compile submitted code itself. Configure a separately isolated,
trusted Judge0 deployment before testing execution:

```bash
export JUDGE0_BASE_URL="http://YOUR-ISOLATED-JUDGE0:2358"
# Optional, depending on your Judge0 provider:
export JUDGE0_API_KEY="..."
# Choose an appropriate installed language ID for your Judge0 instance.
# Default 62 is a Java language ID on some installations, not guaranteed Java 21.
export JUDGE0_LANGUAGE_ID=62
cd backend
mvn spring-boot:run
```

This feature needs an externally deployed and securely configured Judge0 instance; it will
return HTTP 503 when no endpoint is configured. Use a Java `public class Main` with a
`main` entry point reading stdin and printing stdout. Example input formats:
- Two Sum: number of elements, array values, target (one per line); output two indices separated by space.
- Valid Parentheses: bracket string; output `true` or `false`.
- Maximum Subarray: number of elements, array values; output maximum sum.

**Not production-ready:** The sample API currently has no login, persistent runs, distributed
rate limit, or budget enforcement. Restrict it to trusted local development and do not expose
it publicly until authentication, quotas, outbound provider allowlisting, and sandbox controls
are independently verified. Judge0's execution isolation is a separate deployment concern.
