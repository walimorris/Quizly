package com.morris.quizly.models.quiz;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizlyQuestionGroup {

    @NotBlank
    private String question;

    @NotNull
    @Min(4)
    @Max(4)
    private List<String> options;

    @NotBlank
    private String answer;
}
