package com.codeforge.judge;

import com.codeforge.problem.ProblemController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/runs")
public class RunController {
    private static final Duration TTL = Duration.ofMinutes(20);
    private static final int MAX_RUNS = 1000;
    private final Map<UUID, PendingRun> runs = new ConcurrentHashMap<>();
    private final ProblemController problems;
    private final Judge0Client judge;

    public RunController(ProblemController problems, Judge0Client judge) {
        this.problems = problems;
        this.judge = judge;
    }

    public record RunRequest(@NotNull Long problemId, @NotBlank @Size(max = 16000) String sourceCode) {}
    public record RunCreated(UUID runId, String state) {}
    public record RunResult(UUID runId, String state, String detail, String stdout,
                            String stderr, String compileOutput) {}
    private record PendingRun(String judgeToken, Instant createdAt, RunResult cached) {}

    /** Evaluates exactly ONE public sample. This is not a verified submission. */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RunCreated runSample(@Valid @RequestBody RunRequest request) {
        var problem = problems.byId(request.problemId());
        expireOldRuns();
        if (runs.size() >= MAX_RUNS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Too many active sample runs; please try again later");
        }
        String token = judge.submit(request.sourceCode(), problem.sampleInput(), problem.sampleOutput());
        UUID id = UUID.randomUUID();
        runs.put(id, new PendingRun(token, Instant.now(), null));
        return new RunCreated(id, "RUNNING");
    }

    @GetMapping("/{runId}")
    public RunResult runStatus(@PathVariable UUID runId) {
        expireOldRuns();
        PendingRun run = runs.get(runId);
        if (run == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Sample run not found or expired");
        if (run.cached() != null) return run.cached();
        Judge0Client.Verdict verdict = judge.result(run.judgeToken());
        if (verdict.statusId() == 1 || verdict.statusId() == 2) {
            return new RunResult(runId, "RUNNING", "Queued or processing", null, null, null);
        }
        String state = switch (verdict.statusId()) {
            case 3 -> "SAMPLE_PASSED";
            case 4 -> "SAMPLE_FAILED";
            case 5 -> "TIME_LIMIT_EXCEEDED";
            case 6 -> "COMPILATION_ERROR";
            default -> "EXECUTION_ERROR";
        };
        var result = new RunResult(runId, state, limit(verdict.description()),
            limit(verdict.stdout()), limit(verdict.stderr()), limit(verdict.compileOutput()));
        runs.computeIfPresent(runId, (id, original) ->
            new PendingRun(original.judgeToken(), original.createdAt(), result));
        return result;
    }

    private void expireOldRuns() {
        Instant expiry = Instant.now().minus(TTL);
        runs.entrySet().removeIf(e -> e.getValue().createdAt().isBefore(expiry));
    }

    private static String limit(String text) {
        return text == null ? null : text.substring(0, Math.min(text.length(), 2000));
    }
}
