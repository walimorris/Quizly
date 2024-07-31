package com.morris.quizly.models.system;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/**
 * Quizly's system AI provides a functional interface for various LLM providers. This interface utilizes general
 * templates that can be used across different providers and languages.
 * <br><br>
 * Review prompt templates at <b>backend/src/main/resources/prompts</b>
 */
@AiService
public interface SystemAi {

    /**
     * General OpenAi Quiz prompt for the English language.
     *
     * @param userMessage prompt for AI system that contains the user input
     * @return {@link String} AI system response
     */
    @UserMessage(fromResource = "/prompts/basic-quiz-format-en.txt")
    String openAiQuizPromptEN(@V("message") String userMessage);

    /**
     * General OpenAi Quiz prompt for the Bulgarian language.
     *
     * @param userMessage prompt for AI system that contains the user input
     * @return {@link String} AI system response
     */
    @UserMessage(fromResource = "/prompts/basic-quiz-format-bg.txt")
    String openAiQuizPromptBG(@V("message") String userMessage);
}
