package com.codeforge.problem;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/problems")
@CrossOrigin(origins = "http://localhost:5173")
public class ProblemController {
    public record Problem(Long id, String title, String difficulty, List<String> topics,
                          String description, String examples) {}

    private static final List<Problem> PROBLEMS = List.of(
        new Problem(1L, "Two Sum", "Easy", List.of("Arrays", "Hashing"),
            "Given an integer array nums and a target, return indices of two distinct elements whose sum equals target.",
            "nums = [2,7,11,15], target = 9 → [0,1]"),
        new Problem(2L, "Valid Parentheses", "Easy", List.of("Stacks", "Strings"),
            "Given a string containing ()[]{} characters, determine whether every bracket is closed in the correct order.",
            "s = \"([])\" → true"),
        new Problem(3L, "Maximum Subarray", "Medium", List.of("Arrays", "Dynamic Programming"),
            "Find the maximum sum of a non-empty contiguous subarray.",
            "nums = [-2,1,-3,4,-1,2,1,-5,4] → 6")
    );

    @GetMapping
    public List<Problem> all() { return PROBLEMS; }

    @GetMapping("/{id}")
    public Problem byId(@PathVariable Long id) {
        return PROBLEMS.stream().filter(p -> p.id().equals(id)).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
    }
}
