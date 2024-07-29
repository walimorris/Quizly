import React, { useEffect } from 'react';
import { getUserDetails } from '../utils/auth';
import QuizList from './QuizList';
import ResponsiveAppBar from "./ResponsiveAppBar";
import Container from '@mui/material/Container';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import CssBaseline from '@mui/material/CssBaseline';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { useTranslation } from 'react-i18next';
import Footer from "./Footer";

const defaultTheme = createTheme({
    palette: {
        background: {
            default: "#f8f8f8" // off-white with a slight gray tint
        }
    }
});

const Documents = ({ changeLanguage }) => {
    const { t, i18n } = useTranslation();
    const userDetails = getUserDetails();

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userDetails');
        window.location.href = '/login';
    };

    useEffect(() => {
        const savedLanguage = localStorage.getItem('language');
        if (savedLanguage) {
            i18n.changeLanguage(savedLanguage);
        }
    }, []);

    return (
        <ThemeProvider theme={defaultTheme}>
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
                        <Grid item xs={12} md={12}>
                            <QuizList userId={userDetails._id} />
                        </Grid>
                    </Grid>
                </Box>
            </Container>
            <Footer/>
        </ThemeProvider>
    );
};

export default Documents;
