import * as React from 'react';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CardMedia from '@mui/material/CardMedia';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';
import { useTranslation } from 'react-i18next';
import Card from "@mui/material/Card";

const StyledCard = styled(Card)(({ theme }) => ({
    transition: 'transform 0.2s, box-shadow 0.2s',
    '&:hover': {
        transform: 'scale(1.05)',
        boxShadow: theme.shadows[5],
    },
}));

export default function DocumentMediaCard({ quiz, onClick }) {
    const { t } = useTranslation();

    const handleCardClick = () => {
        const pdfBlob = new Blob([Uint8Array.from(atob(quiz.pdfContent), c => c.charCodeAt(0))], { type: 'application/pdf' });
        const pdfUrl = URL.createObjectURL(pdfBlob);
        onClick(pdfUrl);
    };

    return (
        <StyledCard onClick={() => handleCardClick(quiz.pdfContent)}>
            <CardMedia
                sx={{ height: 140 }}
                image={quiz.pdfImage}
                title={quiz.quizTitle}
            />
            <CardContent>
                <Typography gutterBottom variant="h5" component="div">
                    {quiz.quizTitle}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    {t('created on')}: {new Date(quiz.createdDate).toLocaleDateString()}
                </Typography>
            </CardContent>
            <CardActions>
                <Button size="small">{t('share')}</Button>
                <Button size="small">{t('learn more')}</Button>
            </CardActions>
        </StyledCard>
    );
}



