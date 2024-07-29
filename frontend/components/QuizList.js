import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Grid from '@mui/material/Grid';
import Container from '@mui/material/Container';
import Button from '@mui/material/Button';
import DocumentMediaCard from "./cards/DocumentMediaCard";
import { useTranslation } from 'react-i18next';
import PdfModal from './PdfModal';
import Box from "@mui/material/Box";

const QuizList = ({ userId }) => {
    const { t, i18n } = useTranslation();
    const [quizzes, setQuizzes] = useState([]);
    const [error, setError] = useState(null);
    const [pdfUrl, setPdfUrl] = useState('');
    const [modalOpen, setModalOpen] = useState(false);
    const [page, setPage] = useState(0);
    const [size] = useState(9);
    const [hasMore, setHasMore] = useState(true);

    useEffect(() => {
        const fetchQuizzes = async (page) => {
            try {
                const response = await axios.get(`/api/quiz-retrieval/${userId}/quizzes`, {
                    params: { page, size }
                });
                console.log(response.data);
                const { _embedded, page: pageInfo } = response.data;
                const content = _embedded ? _embedded.quizList : [];

                if (content.length === 0 || pageInfo.number >= pageInfo.totalPages - 1) {
                    setHasMore(false);
                    if (content.length > 0) {
                        setQuizzes(prevQuizzes => [...prevQuizzes, ...content]);
                    }
                } else {
                    setQuizzes(prevQuizzes => [...prevQuizzes, ...content]);
                }
            } catch (error) {
                setError(error.message);
            }
        };

        const savedLanguage = localStorage.getItem('language');
        if (savedLanguage) {
            i18n.changeLanguage(savedLanguage);
        }

        fetchQuizzes(page);
    }, []);

    const loadMoreQuizzes = () => {
        setPage(prevPage => prevPage + 1);
    };

    const handleCardClick = (pdfUrl) => {
        setPdfUrl(pdfUrl);
        setModalOpen(true);
    };

    const handleClose = () => {
        setModalOpen(false);
        setPdfUrl('');
    };

    if (error) {
        return <div>{t('error')}: {error}</div>;
    }

    if (!quizzes.length) {
        return (
            <Grid container spacing={2} justifyContent="center">
            <Grid item xs={12} md={10}>
                <Box
                    sx={{
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        minHeight: '60vh'
                    }}
                >
                    <l-quantum size="60" speed="1.75" color="black" />
                </Box>
            </Grid>
        </Grid>);
    }

    return (
        <Container>
            <Grid container spacing={3}>
                {quizzes.map((quiz) => (
                    <Grid item xs={12} sm={6} md={4} key={quiz._id}>
                        <DocumentMediaCard quiz={quiz} onClick={handleCardClick} />
                    </Grid>
                ))}
            </Grid>
            {hasMore && (
                <Box my={4} display="flex" justifyContent="center">
                    <Button onClick={loadMoreQuizzes} variant="contained" color="primary">
                        {t('load_more')}
                    </Button>
                </Box>
            )}
            <PdfModal open={modalOpen} handleClose={handleClose} pdfUrl={pdfUrl} />
        </Container>
    );
};

export default QuizList;





