import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { quantum } from "ldrs";
import Grid from "@mui/material/Grid";
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Container from '@mui/material/Container';
import CssBaseline from '@mui/material/CssBaseline';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import QuizCard from "./cards/QuizCard";
import { getUserDetails } from '../utils/auth';
import ResponsiveAppBar from './ResponsiveAppBar';
import { useTranslation } from 'react-i18next';
import Typography from '@mui/material/Typography';
import { Radio, RadioGroup } from "@mui/material";
import FormControlLabel from "@mui/material/FormControlLabel";
import Confetti from 'react-confetti';
import { useWindowSize } from 'react-use';
import Footer from "./Footer";

const defaultTheme = createTheme({
    palette: {
        background: {
            default: "#f8f8f8" // off-white with a slight gray tint
        }
    }
});

const Dashboard = ({ changeLanguage }) => {
    const { t, i18n } = useTranslation();
    const [numberOfQuestions, setNumberOfQuestions] = useState('');
    const [prompt, setPrompt] = useState('');
    const [quizTitle, setQuizTitle] = useState('');
    const [showCradle, setShowCradle] = useState(false);
    const [userDetails, setUserDetails] = useState(getUserDetails());
    const [quizData, setQuizData] = useState(null);
    const [userAnswers, setUserAnswers] = useState({});
    const [score, setScore] = useState(null);
    const [showConfetti, setShowConfetti] = useState(false);
    const { width, height } = useWindowSize();
    const [viewHeight, setViewHeight] = useState(window.innerHeight);
    const [lockedAnswers, setLockedAnswers] = useState(false);
    const [correctAnswers, setCorrectAnswers] = useState({});
    const [isGeneratingQuiz, setIsGeneratingQuiz] = useState(false);
    const [errorMessage, setErrorMessage] = useState(''); // State for error message

    useEffect(() => {
        const savedLanguage = localStorage.getItem('language');
        if (savedLanguage) {
            i18n.changeLanguage(savedLanguage);
        }
        const handleResize = () => {
            setViewHeight(window.innerHeight);
        };
        window.addEventListener('resize', handleResize);
        return () => {
            window.removeEventListener('resize', handleResize);
        };
    }, []);

    quantum.register();

    const handleSubmit = async (event) => {
        event.preventDefault();
        setShowCradle(true);
        setShowConfetti(false);
        setIsGeneratingQuiz(true);
        setErrorMessage(''); // Clear any previous error message
        console.log("language: ", i18n.language);

        const quizRequest = {
            numberOfQuestions,
            prompt,
            quizTitle,
            userId: userDetails._id,
            language: i18n.language
        };

        try {
            const response = await axios.post('/api/quiz-generation/basic', quizRequest);
            setNumberOfQuestions('');
            setPrompt('');
            setQuizTitle('');
            setQuizData(response.data);
            setScore(null); // Reset score when a new quiz is generated
            setLockedAnswers(false);
            setCorrectAnswers({});
        } catch (error) {
            console.error('Error generating quiz:', error);
            if (error.response && error.response.data === "access denied") {
                setErrorMessage('access_denied');
                setNumberOfQuestions('');
                setPrompt('');
                setQuizTitle('');
            }
        } finally {
            setShowCradle(false);
            setIsGeneratingQuiz(false);
        }
    };

    const handleAnswerChange = (questionIndex, option) => {
        if (lockedAnswers) return;
        setUserAnswers(prevState => ({
            ...prevState,
            [questionIndex]: option
        }));
    };

    // Let's make sure we're trimming whitespace, making all cases the same, and removing termination characters
    // We lowercase here first in order to change any possible answer letters ( ex: A. database) before we remove
    const cleanAnswer = (str) => {
        str = str.replace(/[.,;!?]$/, '').trim().toLowerCase();
        return str.replace(/^[a-zA-Z\u0400-\u04FF]\.?\s+/, ''); // also checks bulgarian chars
    };

    const handleQuizSubmit = (event) => {
        event.preventDefault();
        if (!quizData) return;

        let correctAnswersCount = 0;
        const newCorrectAnswers = {};

        quizData.forEach((question, index) => {
            const correctAnswer = cleanAnswer(question.answer);
            const userAnswer = cleanAnswer(userAnswers[index] || '');
            console.log(quizData)
            console.log(correctAnswer);
            console.log(userAnswer);

            if (userAnswer === correctAnswer) {
                correctAnswersCount++;
            } else {
                newCorrectAnswers[index] = correctAnswer;
            }
        });

        setScore(correctAnswersCount);
        setLockedAnswers(true);
        setCorrectAnswers(newCorrectAnswers);

        if (correctAnswersCount / quizData.length >= 0.8) {
            setShowConfetti(true);
            setTimeout(() => {
                setShowConfetti(false);
            }, 30000);
        }
    };

    const handleQuizReset = () => {
        setUserAnswers({});
        setScore(null);
        setLockedAnswers(false);
        setCorrectAnswers({});
        setShowConfetti(false);
    };

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userDetails');
        window.location.href = '/login';
    };

    return (
        <ThemeProvider theme={defaultTheme}>
            {showConfetti && (
                <Confetti
                    width={width}
                    height={viewHeight}
                    style={{
                        position: 'fixed',
                        top: '0',
                        left: '0',
                        zIndex: 9999
                    }}
                />
            )}
            <ResponsiveAppBar userDetails={userDetails} handleLogout={handleLogout} changeLanguage={changeLanguage} />
            <Container component="main" sx={{ backgroundColor: 'background.default', minHeight: '100vh', position: 'relative' }}>
                <CssBaseline />
                <Box
                    sx={{
                        marginTop: 8,
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                    }}
                >
                    <Grid container spacing={2} justifyContent="center">
                        <Grid item xs={12} md={12}> {/* Adjust the width of the QuizCard component */}
                            <QuizCard />
                        </Grid>
                    </Grid>
                    <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3, width: '100%' }}>
                        <Grid container spacing={2} justifyContent="center">
                            <Grid item xs={12} md={7}>
                                <TextField
                                    name="numberOfQuestions"
                                    required
                                    fullWidth
                                    id="numberOfQuestions"
                                    label={t('number of questions')}
                                    value={numberOfQuestions}
                                    onChange={(e) => setNumberOfQuestions(Number(e.target.value))}
                                    inputProps={{ min: "1" }}
                                />
                            </Grid>
                            <Grid item xs={12} md={7}>
                                <TextField
                                    name="prompt"
                                    required
                                    fullWidth
                                    id="prompt"
                                    label={t('prompt')}
                                    value={prompt}
                                    onChange={(e) => setPrompt(e.target.value)}
                                />
                            </Grid>
                            <Grid item xs={12} md={7}>
                                <TextField
                                    name="quizTitle"
                                    required
                                    fullWidth
                                    id="quizTitle"
                                    label={t('quiz title')}
                                    value={quizTitle}
                                    onChange={(e) => setQuizTitle(e.target.value)}
                                />
                            </Grid>
                        </Grid>
                        <Grid container spacing={2} justifyContent="center">
                            <Grid item xs={12} md={7}>
                                <Button
                                    type="submit"
                                    fullWidth
                                    variant="contained"
                                    sx={{ mt: 3, mb: 2 }}
                                    disabled={isGeneratingQuiz}
                                >
                                    {t('generate quiz')}
                                </Button>
                            </Grid>
                        </Grid>
                        {showCradle && (
                            <Grid container spacing={2} justifyContent="center">
                                <Grid item xs={12} md={10}>
                                    <Box
                                        sx={{
                                            display: 'flex',
                                            justifyContent: 'center',
                                            alignItems: 'center',
                                            minHeight: '20vh'
                                        }}
                                    >
                                        <l-quantum size="60" speed="1.75" color="black" />
                                    </Box>
                                </Grid>
                            </Grid>
                        )}
                    </Box>
                    {errorMessage && (
                        <Typography variant="body1" color="error" sx={{ mt: 2 }}>
                            {t(errorMessage)}
                        </Typography>
                    )}
                    {quizData && (
                        <Box
                            component="form"
                            noValidate
                            onSubmit={handleQuizSubmit}
                            sx={{
                                mt: 3,
                                width: '80%',
                                backgroundColor: '#f0f0f0',
                                padding: '20px',
                                borderRadius: '8px',
                                boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
                                textAlign: 'center'
                            }}
                        >
                            {quizData.map((question, index) => (
                                <Box key={index} sx={{ mb: 4, textAlign: 'left' }}>
                                    <Typography variant="h6">{question.question}</Typography>
                                    <RadioGroup
                                        value={userAnswers[index] || ''}
                                        onChange={(e) => handleAnswerChange(index, e.target.value)}
                                    >
                                        {question.options.map((option, i) => {
                                            const userAnswer = cleanAnswer(userAnswers[index] || '');
                                            const correctAnswer = cleanAnswer(question.answer);

                                            const cleanedOption = cleanAnswer(option);
                                            const isCorrectAnswer = cleanedOption === correctAnswer;
                                            const isUserSelected = cleanedOption === userAnswer;

                                            let color = 'inherit';
                                            let fontWeight = 'normal';

                                            if (lockedAnswers) {
                                                if (isCorrectAnswer && isUserSelected) {
                                                    color = 'green';
                                                    fontWeight = 'bold'; // correct and selected by the user
                                                } else if (isCorrectAnswer && !isUserSelected) {
                                                    color = 'red'; // Correct but not selected by the user
                                                    fontWeight = 'bold';
                                                } else if (!isCorrectAnswer && isUserSelected) {
                                                    fontWeight = 'bold'; // Incorrect and selected by the user
                                                }
                                            }

                                            return (
                                                <FormControlLabel
                                                    key={i}
                                                    value={option}
                                                    control={<Radio />}
                                                    label={option}
                                                    style={{ color, fontWeight }}
                                                />
                                            );
                                        })}
                                    </RadioGroup>
                                </Box>
                            ))}
                            {score !== null && (
                                <Box sx={{ mb: 4 }}>
                                    <Typography variant="h5">
                                        {t('your score')}: {((score / quizData.length) * 100).toFixed(2)}%
                                    </Typography>
                                </Box>
                            )}
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', width: '80%', mx: 'auto' }}>
                                <Button
                                    type="submit"
                                    variant="contained"
                                    sx={{ mt: 3, mb: 2, width: '48%' }}
                                >
                                    {t('submit')}
                                </Button>
                                <Button
                                    type="button"
                                    variant="contained"
                                    onClick={handleQuizReset}
                                    sx={{ mt: 3, mb: 2, width: '48%' }}
                                >
                                    {t('reset')}
                                </Button>
                            </Box>
                        </Box>
                    )}
                </Box>
            </Container>
            <Footer/>
        </ThemeProvider>
    );
};

export default Dashboard;
