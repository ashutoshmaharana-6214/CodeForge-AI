package com.codeforge.problem;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ProblemControllerTest {
    private final ProblemController controller = new ProblemController();

    @Test
    void seededProblemCatalogHasDistinctIds() {
        var problems = controller.all();
        assertThat(problems).hasSize(3);
        assertThat(problems.stream().map(ProblemController.Problem::id).distinct()).hasSize(3);
    }

    @Test
    void lookupReturnsMatchingProblem() {
        assertThat(controller.byId(1L).title()).isEqualTo("Two Sum");
    }
}
