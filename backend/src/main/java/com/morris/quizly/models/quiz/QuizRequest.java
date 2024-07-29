package com.morris.quizly.models.quiz;

import com.morris.quizly.models.locales.Language;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Recommended use for Quiz generation activities. Generated quizzes expect title, number of generated questions,
 * a prompt, and the required language of the generated quiz. The userId is an option, in the case of generating
 * quizzes for a specific user.
 */
@Getter
@Setter
@ToString
@Builder
public class QuizRequest {
    private String userId;

    @NotBlank
    private String quizTitle;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer numberOfQuestions;

    @NotBlank
    private String prompt;

    @NotNull
    private Language language;
}
