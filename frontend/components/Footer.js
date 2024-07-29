import * as React from 'react';
import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Link from '@mui/material/Link';
import {useTranslation} from "react-i18next";

function Copyright() {
    const { t } = useTranslation();

    return (
        <Typography variant="body2" color="text.secondary" align="center">
            { t('copyright') + ' © '}
            <Link color="inherit" href="https://mui.com/" target='_blank'>
                quizly
            </Link>{' '}
            {new Date().getFullYear()}
            {'.'}
        </Typography>
    );
}

function Footer() {
    return (
        <Box component="footer" sx={{ bgcolor: 'background.paper', py: 6 }}>
            <Container maxWidth="lg">
                <Copyright />
            </Container>
        </Box>
    );
}

export default Footer;
