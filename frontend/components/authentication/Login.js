import React, {useState, useRef, useEffect} from 'react';
import axios from 'axios';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Link from '@mui/material/Link';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Typography from '@mui/material/Typography';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { useTranslation } from 'react-i18next';

function Copyright(props) {
    const { t } = useTranslation();
    const copyright = 'copyright';
    return (
        <Typography variant="body2" color="text.secondary" align="center" {...props}>
            { t(copyright) + ' © '}
            <Link color="inherit" href="https://mui.com/">
                quizly
            </Link>{' '}
            {new Date().getFullYear()}
            {'.'}
        </Typography>
    );
}

const defaultTheme = createTheme({
    palette: {
        background: {
            default: "#f8f8f8" // off-white with a slight gray tint
        }
    }
});

export default function Login() {
    const { t, i18n } = useTranslation();
    const formRef = useRef(null);
    const [error, setError] = useState('');

    React.useEffect(() => {
        const savedLanguage = localStorage.getItem('language');
        if (savedLanguage) {
            i18n.changeLanguage(savedLanguage);
        }
    }, []);

    const handleSubmit = (event) => {
        event.preventDefault();
        const form = formRef.current;
        const data = new FormData(form);

        axios.post('/api/auth/login', data, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }).then(response => {
            // Store JWT tokens and user details in local storage or cookies
            const { accessToken, refreshToken, userDetails } = response.data;
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);
            localStorage.setItem('userDetails', JSON.stringify(userDetails));
            form.reset();
            window.location.href = "/dashboard";
        }).catch(error => {
            form.reset();
            setError(t(error.response?.data.error));
            console.error('Login Error: ', error.response?.data || error.message);
        });
    };

    const handleLanguageChange = (language) => {
        i18n.changeLanguage(language);
        localStorage.setItem('language', language); // Save the selected language to local storage
    };

    return (
        <ThemeProvider theme={defaultTheme}>
            <Grid container component="main" sx={{ backgroundColor: 'background.default', minHeight: '100vh' }}>
                <CssBaseline />
                <Grid
                    item
                    xs={false}
                    sm={4}
                    md={7}
                    sx={{
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        backgroundColor: 'rgba(0, 0, 0, 0.3)',
                    }}
                >
                    <img
                        src="/images/quizly-logo.png" // Replace with the actual path to your logo
                        alt="Quizly Logo"
                        style={{ maxWidth: '50%', height: 'auto' }}
                    />
                </Grid>
                <Grid item xs={12} sm={8} md={5} component={Paper} elevation={6} square>
                    <Box
                        sx={{
                            my: 8,
                            mx: 4,
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                        }}
                    >
                        <Avatar sx={{ m: 1, bgcolor: 'secondary.main' }}>
                            <LockOutlinedIcon />
                        </Avatar>
                        <Typography component="h1" variant="h5">
                            {t('sign in')}
                        </Typography>
                        <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 1 }} ref={formRef}>
                            <TextField
                                margin="normal"
                                required
                                fullWidth
                                id="emailAddress"
                                label={t('email address')}
                                name="emailAddress"
                                autoComplete="email"
                                autoFocus
                            />
                            <TextField
                                margin="normal"
                                required
                                fullWidth
                                name="password"
                                label={t('password')}
                                type="password"
                                id="password"
                                autoComplete="current-password"
                            />
                            <FormControlLabel
                                control={<Checkbox value="remember" color="primary" />}
                                label={t('remember me')}
                            />
                            {error && (
                                <Typography color="error" variant="body2">
                                    {error}
                                </Typography>
                            )}
                            <Button
                                type="submit"
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2 }}
                            >
                                {t('sign in')}
                            </Button>
                            <Grid container>
                                <Grid item xs>
                                    <Link href="#" variant="body2">
                                        {t('forgot password')}
                                    </Link>
                                </Grid>
                                <Grid item>
                                    <Link href="/signup" variant="body2">
                                        {t("don't have an account? sign up")}
                                    </Link>
                                </Grid>
                            </Grid>
                            <Copyright sx={{ mt: 5 }} />
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
                </Grid>
            </Grid>
        </ThemeProvider>
    );
}
