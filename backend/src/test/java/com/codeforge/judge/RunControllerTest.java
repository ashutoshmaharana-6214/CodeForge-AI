package com.codeforge.judge;

import com.codeforge.problem.ProblemController;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class RunControllerTest {
    private final Judge0Client judge = mock(Judge0Client.class);
    private final RunController controller = new RunController(new ProblemController(), judge);

    @Test
    void sendsOnlyThePublicExampleToTheIsolatedJudge() {
        when(judge.submit(anyString(), anyString(), anyString())).thenReturn("provider-token");
        var created = controller.runSample(new RunController.RunRequest(1L, "public class Main {}"));
        assertThat(created.state()).isEqualTo("RUNNING");
        verify(judge).submit("public class Main {}", "4\n2 7 11 15\n9\n", "0 1\n");
    }

    @Test
    void successfulSampleDoesNotClaimAFullAcceptedSubmission() {
        when(judge.submit(anyString(), anyString(), anyString())).thenReturn("provider-token");
        when(judge.result("provider-token")).thenReturn(
            new Judge0Client.Verdict(3, "Accepted", "0 1\n", null, null));
        var created = controller.runSample(new RunController.RunRequest(1L, "public class Main {}"));
        var result = controller.runStatus(created.runId());
        assertThat(result.state()).isEqualTo("SAMPLE_PASSED");
        assertThat(controller.runStatus(created.runId())).isEqualTo(result);
        verify(judge, times(1)).result("provider-token");
    }

    @Test
    void reportsCompilationErrorWithoutMarkingSamplePassed() {
        when(judge.submit(anyString(), anyString(), anyString())).thenReturn("provider-token");
        when(judge.result("provider-token")).thenReturn(
            new Judge0Client.Verdict(6, "Compilation Error", null, null, "Syntax error"));
        var created = controller.runSample(new RunController.RunRequest(2L, "invalid"));
        var result = controller.runStatus(created.runId());
        assertThat(result.state()).isEqualTo("COMPILATION_ERROR");
        assertThat(result.compileOutput()).isEqualTo("Syntax error");
    }

    @Test
    void missingProblemCannotReachTheJudge() {
        assertThatThrownBy(() -> controller.runSample(
            new RunController.RunRequest(999L, "anything")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND));
        verifyNoInteractions(judge);
    }
}
