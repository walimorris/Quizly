import * as React from 'react';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CardMedia from '@mui/material/CardMedia';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { useTranslation } from 'react-i18next';

export default function QuizCard() {
    const { t } = useTranslation();

    return (
        <Card sx={{ width: '60%', marginTop: '2%', marginLeft: 'auto', marginRight: 'auto'}}>
            <CardMedia
                sx={{ height: 235 }}
                image="/images/quiz-gen-card.png"
                title="quiz-generation"
            />
            <CardContent>
                <Typography gutterBottom variant="h5" component="div">
                    {t('quiz and document generation')}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    {t('Quizly uses AI to generate quizzes based on a given domain. Example: if I want to generate a quiz to study for an upcoming MongoDB Developer Certification. I simply input, "MongoDB Developer Certification" and quizly will generate something that\'ll help me pass MongoDB\'s Developer Cert. If you want to review these quizzes later, you can save or download them for review.')}
                </Typography>
            </CardContent>
            <CardActions>
                <Button target='_blank' href="https://openai.com/" size="small">{t('learn more')}</Button>
            </CardActions>
        </Card>
    );
}

