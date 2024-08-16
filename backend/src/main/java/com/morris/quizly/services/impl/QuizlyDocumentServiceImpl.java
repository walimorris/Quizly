package com.morris.quizly.services.impl;

import com.morris.quizly.models.locales.Language;
import com.morris.quizly.models.quiz.Quiz;
import com.morris.quizly.models.quiz.QuizRequest;
import com.morris.quizly.models.quiz.QuizlyQuestionGroup;
import com.morris.quizly.repositories.QuizRepository;
import com.morris.quizly.services.OpenAiService;
import com.morris.quizly.services.QuizlyDocumentService;
import com.morris.quizly.utils.FileUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.morris.quizly.models.locales.Language.BG;
import static com.morris.quizly.models.locales.Language.EN;

@Service
public class QuizlyDocumentServiceImpl implements QuizlyDocumentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuizlyDocumentServiceImpl.class);

    private final OpenAiService openAiService;
    private final QuizRepository quizRepository;

    private static final String DEJAVU_SANS_FONT = "/fonts/DejaVuSans.ttf";

    private static final String QUIZ_SPLIT_REGEX_EN = "Answer Key:";
    private static final String QUIZ_TITLE_REGEX_EN = "Quiz Title: ";
    private static final String QUIZ_SPLIT_REGEX_BG = "Ключ за отговори:";
    private static final String QUIZ_TITLE_REGEX_BG = "Заглавие на теста: ";
    private static final String NO_ANSWER_PROVIDED = "No answer provided";

    @Autowired
    public QuizlyDocumentServiceImpl(QuizRepository quizRepository, OpenAiService openAiService) {
        this.quizRepository = quizRepository;
        this.openAiService = openAiService;
    }

    @Override
    public Page<Quiz> getQuizzesByUserIdDescending(String userId, Pageable pageable) {
        try {
            return quizRepository.findQuizzesByUserIdDescending(userId, pageable);
        } catch (Exception e) {
            LOGGER.info("Error finding quizzes with id '{}': {}", userId, e.getMessage());
            return null;
        }
    }

    @Override
    public List<QuizlyQuestionGroup> generateQuizlyPDF(String text, QuizRequest quizRequest) {
        String QUIZ_SPLIT_REGEX = "";

        if (quizRequest.getLanguage().equals(EN)) {
            QUIZ_SPLIT_REGEX = QUIZ_SPLIT_REGEX_EN;
        } else if (quizRequest.getLanguage().equals(BG)) {
            QUIZ_SPLIT_REGEX = QUIZ_SPLIT_REGEX_BG;
        }

        // Quizzes can be in different languages, therefore we must split the generated quiz at the Answer Key
        // based on the language. Once this split occurs we can begin generating the document by each of the
        // sections. Questions section and Answer Key section.
        String[] quizSections = text.split(QUIZ_SPLIT_REGEX);
        String questionGroup = quizSections[0].trim();
        String answerKey = quizSections[1].trim();
        List<QuizlyQuestionGroup> quizlyQuestionGroup = parseQuiz(questionGroup, answerKey);
        byte[] pdfContent = generatePdf(quizlyQuestionGroup, quizRequest.getQuizTitle(), answerKey, quizRequest.getLanguage());
        String pdfImage = convertPdfPageToImage(pdfContent);
        List<Double> pdfEmbeddings = openAiService.embedWithOpenAiAda002TextEmbeddings(pdfContent);

        Quiz quiz = Quiz.builder()
                .userId(quizRequest.getUserId())
                .quizTitle(quizRequest.getQuizTitle())
                .questionsGroup(quizlyQuestionGroup)
                .pdfContent(pdfContent)
                .pdfImage(pdfImage)
                .createdDate(LocalDateTime.now())
                .language(quizRequest.getLanguage())
                .pdfEmbeddings(pdfEmbeddings)
                .prompt(quizRequest.getPrompt()) // save the prompt that generated the quiz
                .build();

        quizRepository.save(quiz);
        return quizlyQuestionGroup;
    }

    /**
     * The PDF is converted to a png image. Creates a {@link BufferedImage} of the PDF,
     * and utilizes the {@link FileUtil} to pass that buffered image as a
     * {@link ByteArrayOutputStream} to return as a base64encoded string.
     *
     * @param pdfContent byte[] pdfContent
     *
     * @return {@link String} Base64encoded string
     */
    private String convertPdfPageToImage(byte[] pdfContent) {
        try (PDDocument document = PDDocument.load(pdfContent)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);
            return FileUtil.base64EncodePDF(outputStream.toByteArray(), "image/png");
        } catch (IOException e) {
            LOGGER.error("Error generating image of pdf: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses the quiz and creates a {@link List<QuizlyQuestionGroup>}. This method extracts the
     * question portion of the generated quiz and creates a {@link QuizlyQuestionGroup} for each
     * question. Each {@link QuizlyQuestionGroup} contains: the question, the options, and the
     * answer for the question.
     *
     * @param questionGroup {@link String} generated quiz questions, options, and answer
     * @param answerKey     {@link String} generated quiz answer key
     *
     * @return {@link List<QuizlyQuestionGroup>} containing the question, options, and answer for each question
     */
    private List<QuizlyQuestionGroup> parseQuiz(String questionGroup, String answerKey) {
        List<QuizlyQuestionGroup> questions = new ArrayList<>();

        // Regular expression to match each question and its options with an additional space between questions
        Pattern questionPattern = Pattern.compile("(\\d+\\.\\s+[^\\n]+)\\n\\s*(A\\.\\s+[^\\n]+)\\n\\s*(B\\.\\s+[^\\n]+)\\n\\s*(C\\.\\s+[^\\n]+)\\n\\s*(D\\.\\s+[^\\n]+)\\n?", Pattern.MULTILINE);
        Matcher questionMatcher = questionPattern.matcher(questionGroup);

        // Regular expression to match each answer in the answer key
        Pattern answerKeyPattern = Pattern.compile("(\\d+)\\.\\s*([^\\n]+)");
        List<String> answers = getAnswers(answerKey, answerKeyPattern);

        int index = 0;
        while (questionMatcher.find()) {
            String questionText = questionMatcher.group(1).trim();
            String optionA = questionMatcher.group(2).trim();
            String optionB = questionMatcher.group(3).trim();
            String optionC = questionMatcher.group(4).trim();
            String optionD = questionMatcher.group(5).trim();

            List<String> options = Arrays.asList(optionA, optionB, optionC, optionD);

            // Retrieve the answer from the parsed answer key
            String answer = answers.size() > index ? answers.get(index) : NO_ANSWER_PROVIDED;

            QuizlyQuestionGroup group = QuizlyQuestionGroup.builder()
                    .question(questionText)
                    .options(options)
                    .answer(answer)
                    .build();

            questions.add(group);
            index++;
        }
        return questions;
    }

    /**
     * Each answer from the answer key is added to the QuizlyQuestionGroup
     * If no answer is provided there's been a grave mistake (the LLM didn't give
     * an answer to each question, which is unlikely, or the format of the output
     * from the LLM is corrupt which is most likely).
     *
     * @param answerKey        {@link String} answer key section from parsed quiz
     * @param answerKeyPattern {@link Pattern} Regex Pattern to parse answers
     *
     * @return {@link List<String>} answers from answer key
     */
    @NotNull
    private List<String> getAnswers(String answerKey, Pattern answerKeyPattern) {
        Matcher answerKeyMatcher = answerKeyPattern.matcher(answerKey);
        List<String> answers = new ArrayList<>();
        while (answerKeyMatcher.find()) {
            answers.add(answerKeyMatcher.group(2).trim());
        }
        return answers;
    }

    /**
     * Generates a PDF document for a quiz.
     *
     * @param questionGroupList the list of question groups
     * @param quizTitle         the title of the quiz
     * @param answerKey         the answer key
     * @param language          the language of the quiz
     * @return a byte array representing the generated PDF
     */
    public byte[] generatePdf(List<QuizlyQuestionGroup> questionGroupList, String quizTitle, String answerKey, Language language) {
        String quizTitleRegex = "";
        String quizSplitRegex = "";

        if (language.equals(Language.EN)) {
            quizTitleRegex = QUIZ_TITLE_REGEX_EN;
            quizSplitRegex = QUIZ_SPLIT_REGEX_EN;
        } else if (language.equals(Language.BG)) {
            quizTitleRegex = QUIZ_TITLE_REGEX_BG;
            quizSplitRegex = QUIZ_SPLIT_REGEX_BG;
        }

        try (PDDocument document = new PDDocument()) {
            ClassPathResource dejavu = new ClassPathResource(DEJAVU_SANS_FONT);
            PDType0Font font = PDType0Font.load(document, dejavu.getInputStream());

            PDPage currentPage = createNewPage(document);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage)) {
                contentStream.setFont(font, 12);
                contentStream.beginText();
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(50, 750);

                float margin = 50;
                float yStart = 750;
                float yPosition = yStart;

                // Title
                contentStream.showText(quizTitleRegex + quizTitle);
                contentStream.newLine();
                contentStream.newLine();
                yPosition -= (float) (14.5 * 3); // Adjust position

                // Questions
                for (QuizlyQuestionGroup questionGroup : questionGroupList) {
                    yPosition = writeTextWithNewPageCheck(document, contentStream, font, questionGroup.getQuestion(), margin, yStart, yPosition);
                    for (String option : questionGroup.getOptions()) {
                        yPosition = writeTextWithNewPageCheck(document, contentStream, font, option, margin, yStart, yPosition);
                    }
                    contentStream.newLine();
                    yPosition -= 14.5F;
                }
                contentStream.endText();
            }

            // Add second page for answer key
            PDPage answerPage = createNewPage(document);
            try (PDPageContentStream answerContentStream = new PDPageContentStream(document, answerPage)) {
                answerContentStream.setFont(font, 12);
                answerContentStream.beginText();
                answerContentStream.setLeading(14.5f);
                answerContentStream.newLineAtOffset(50, 750);

                answerContentStream.showText(quizSplitRegex);
                answerContentStream.newLine();
                answerContentStream.newLine();

                float yPosition = (float) (750 - 14.5 * 3);
                float margin = 50;
                float yStart = 750;

                String[] answers = answerKey.split("\\r?\\n");
                for (String answer : answers) {
                    yPosition = writeTextWithNewPageCheck(document, answerContentStream, font, answer, margin, yStart, yPosition);
                }
                answerContentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            document.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Error generating PDF", e);
            return null;
        }
    }

    /**
     * Creates a new page for the PDF document.
     *
     * @param document the PDF document
     * @return the newly created page
     */
    private PDPage createNewPage(PDDocument document) {
        PDPage page = new PDPage();
        document.addPage(page);
        return page;
    }


    /**
     * Checks if there is enough space on the current page.
     *
     * @param yPosition the current Y position
     * @param margin    the page margin
     * @return true if there is not enough space, false otherwise
     */
    private boolean outOfSpace(float yPosition, float margin) {
        return yPosition < margin;
    }

    /**
     * Writes text to a PDF content stream, creating a new page if necessary.
     *
     * @param document       the PDF document
     * @param contentStream  the current content stream
     * @param font           the font to use
     * @param text           the text to write
     * @param margin         the page margin
     * @param yStart         the starting Y position
     * @param yPosition      the current Y position
     *
     * @return the updated Y position
     * @throws IOException if an I/O error occurs
     */
    private float writeTextWithNewPageCheck(PDDocument document, PDPageContentStream contentStream, PDType0Font font, String text, float margin, float yStart, float yPosition) throws IOException {
        if (outOfSpace(yPosition, margin)) {
            contentStream.endText();
            contentStream.close();
            PDPage newPage = createNewPage(document);
            contentStream = new PDPageContentStream(document, newPage);
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.setLeading(14.5f);
            contentStream.newLineAtOffset(50, yStart);
            yPosition = yStart;
        }
        contentStream.showText(text);
        contentStream.newLine();
        yPosition -= 14.5F;
        return yPosition;
    }
}
