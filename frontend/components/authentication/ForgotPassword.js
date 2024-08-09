import React, {useEffect, useState} from 'react';
import ReCAPTCHA from 'react-google-recaptcha';
import {createTheme, ThemeProvider} from "@mui/material/styles";
import Container from "@mui/material/Container";
import CssBaseline from "@mui/material/CssBaseline";
import Box from "@mui/material/Box";
import Avatar from "@mui/material/Avatar";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Link from "@mui/material/Link";
import {useTranslation} from "react-i18next";

const defaultTheme = createTheme({
    palette: {
        background: {
            default: "#f8f8f8"
        }
    }
});

export default function ForgotPassword() {
    const { t, i18n } = useTranslation();
    const [recaptchaValue, setRecaptchaValue] = useState(null);
    const [recaptchaVerified, setRecaptchaVerified] = useState(false);
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');

    useEffect(() => {
        const savedLanguage = localStorage.getItem('language');
        if (savedLanguage) {
            i18n.changeLanguage(savedLanguage);
        }
    }, [i18n]);

    const handleLanguageChange = (language) => {
        i18n.changeLanguage(language);
        localStorage.setItem('language', language);
    };

    const handleRecaptchaChange = (value) => {
        setRecaptchaValue(value);
    };

    const handleRecaptchaSubmit = async (e) => {
        e.preventDefault();
        if (recaptchaValue) {
            try {
                const response = await fetch('/api/auth/password-reset/verify-recaptcha', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ token: recaptchaValue }),
                });

                if (response.ok) {
                    setRecaptchaVerified(true);
                } else {
                    setMessage('reCAPTCHA verification failed');
                }
            } catch (error) {
                console.error('Error verifying reCAPTCHA:', error);
                setMessage('Error verifying reCAPTCHA');
            }
        } else {
            setMessage('Please complete the reCAPTCHA');
        }
    };

    const handleEmailSubmit = async (e) => {
        e.preventDefault();
        if (email) {
            // Implement the logic to send a password reset email here.
            console.log('Password reset email sent to:', email);
            try {
                const response = await fetch('/api/auth/password-reset/reset-password', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ emailAddress: email }),
                });

                if (response.ok) {
                    setMessage('Password reset email sent');
                } else {
                    setMessage('Password reset failed');
                }
            } catch (error) {
                console.error('Error sending password reset email:', error);
                setMessage('Error sending password reset');
            }
        } else {
            setMessage('Please enter your email');
        }
    };

    return (
        <ThemeProvider theme={defaultTheme}>
            <Container component="main" maxWidth="xs" sx={{ backgroundColor: 'background.default', minHeight: '100vh'}}>
                <CssBaseline />
                <Box
                    sx={{
                        marginTop: 8,
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                    }}
                >
                    <Avatar sx={{ m: 1, bgcolor: 'secondary.main' }}>
                        <LockOutlinedIcon />
                    </Avatar>
                    <Typography component="h1" variant="h5">
                        {t('forgot_password_page')}
                    </Typography>
                    <Box component="form" noValidate onSubmit={handleEmailSubmit} sx={{ mt: 3 }}>
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <TextField
                                    required
                                    fullWidth
                                    id="email"
                                    label={t('email address')}
                                    name="email"
                                    autoComplete="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <ReCAPTCHA
                                    sitekey="6LfJcx4qAAAAALCKesk_ETIqiqtLHNY8BufzqdW6"
                                    onChange={handleRecaptchaChange}
                                />
                                <Button
                                    fullWidth
                                    variant="contained"
                                    onClick={handleRecaptchaSubmit}
                                    sx={{ mt: 2 }}
                                >
                                    {t('verify_recaptcha')}
                                </Button>
                            </Grid>
                        </Grid>
                        {message && (
                            <Typography color="error" variant="body2" align="center" sx={{ mt: 2 }}>
                                {message}
                            </Typography>
                        )}
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            disabled={!recaptchaVerified}
                            sx={{ mt: 3, mb: 2 }}
                        >
                            {t('send_password_reset_email')}
                        </Button>
                        <Grid container justifyContent="flex-end">
                            <Grid item>
                                <Link href="/login" variant="body2">
                                    {t('remember_password_?_signin')}
                                </Link>
                            </Grid>
                        </Grid>
                    </Box>
                    <Box mt={2} display="flex" justifyContent="center" gap={2}>
                        <Button onClick={() => handleLanguageChange('en')} startIcon={<img src="/images/flag-us.png" alt="US Flag" style={{ width: 20, height: 10 }} />}>
                            English
                        </Button>
                        <Button onClick={() => handleLanguageChange('bg')} startIcon={<img src="/images/flag-bg.png" alt="BG Flag" style={{ width: 20, height: 10 }} />}>
                            Български
                        </Button>
                    </Box>
                </Box>
            </Container>
        </ThemeProvider>
    );
};
