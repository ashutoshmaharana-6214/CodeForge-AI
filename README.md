# CodeForge AI

**Practice. Improve. Master.**

An open-source coding and DSA learning platform with **ForgeMind AI**, our planned tool-using coding mentor.

> **Status: early development.** The technology architecture below is a roadmap; features are not represented as finished until built and tested.

## Project goals

- Topic-based DSA problem library, search, difficulty filters, and a Monaco-powered coding workspace.
- Secure code evaluation with public examples and private judge tests.
- Verified submissions, per-user progress, streaks, and a date-wise activity heatmap.
- ForgeMind AI: contextual hints, debugging assistance, tool calling, RAG, and mock interviews.
- Open-source problem contributions, edge-case fixtures, and automated regression tests.

## Technical architecture

| Technology | Responsibility |
| --- | --- |
| Java 21 + Spring Boot | REST APIs, authentication, problem and submission services |
| React + TypeScript + Monaco | Browser coding interface and activity dashboard |
| Spring AI + LLM + RAG | ForgeMind AI prompts, approved tools, grounded answers |
| PostgreSQL + pgvector | Core relational data and AI knowledge embeddings |
| Redis | Cache and rate-limit state |
| Kafka | Planned asynchronous judging and analytics events |
| Judge0 / isolated workers | Controlled compilation and code execution |
| Docker | Containerized reproducible development |
| Kubernetes | Planned workload orchestration and deployments |
| GitHub Actions | Automated builds, linting, and tests |

Start with a **modular Spring Boot backend**, introducing distributed infrastructure only when working use cases require it.

## Safety and correctness

1. Do not run user-submitted code inside the main Spring Boot process.
2. Never count a problem as solved unless judged against real test cases.
3. Keep secrets and hidden judge test cases outside the public repository.
4. Authenticate and authorize AI tools; enforce execution budgets.
5. Review external contributions and respect upstream software licenses.

## Milestones

- [x] Public GitHub repository initialized.
- [x] Architecture and project roadmap documented.
- [ ] Import and validate the existing Spring Boot + React prototype.
- [ ] Integrate secure code judging and verified verdicts.
- [ ] Add user-specific activity analytics and progress.
- [ ] Integrate ForgeMind's tool-using AI workflows.
- [ ] Implement CI/CD, container packaging, and deployment.

## License

MIT. Contributions are welcome through issues and pull requests.
