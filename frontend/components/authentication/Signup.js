import React, {useState, useRef, useEffect} from 'react';
import axios from 'axios';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Link from '@mui/material/Link';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { useTranslation } from 'react-i18next';
import Footer from "../Footer";

const defaultTheme = createTheme({
    palette: {
        background: {
            default: "#f8f8f8" // off-white with a slight gray tint
        }
    }
});

export default function Signup() {
    const { t, i18n } = useTranslation();
    const formRef = useRef(null);
    const [imageURL, setImageURL] = useState(null);
    const [error, setError] = useState('');
    const [formErrors, setFormErrors] = useState({});

    useEffect(() => {
        const savedLanguage = localStorage.getItem('language');
        if (savedLanguage) {
            i18n.changeLanguage(savedLanguage);
        }
    }, [i18n]);

    const validatePassword = (password) => {
        const errors = {};
        if (password.length < 8) errors.password = t('Password must be at least 8 characters long');
        if (!/[A-Z]/.test(password)) errors.password = t('Password must contain at least one uppercase letter');
        if (!/[a-z]/.test(password)) errors.password = t('Password must contain at least one lowercase letter');
        if (!/[0-9]/.test(password)) errors.password = t('Password must contain at least one digit');
        if (!/[^A-Za-z0-9]/.test(password)) errors.password = t('Password must contain at least one special character');
        return errors;
    };

    const handleChange = (event) => {
        const { name, value, files } = event.target;
        if (name === 'image' && files.length > 0) {
            const file = files[0];
            setImageURL(URL.createObjectURL(file));
        }
    }

    const handleSubmit = (event) => {
        event.preventDefault();
        const form = formRef.current;

        const errors = {};
        if (!form.firstName.value.trim()) errors.firstName = t('First name is required');
        if (!form.lastName.value.trim()) errors.lastName = t('Last name is required');
        if (!form.emailAddress.value.trim()) errors.emailAddress = t('Email address is required');
        const password = form.password.value.trim();
        const passwordErrors = validatePassword(password);
        Object.assign(errors, passwordErrors);

        setFormErrors(errors);

        if (Object.keys(errors).length > 0) {
            event.stopPropagation();
        } else {
            const data = new FormData(form);
            axios.post('/api/auth/signup', data, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            }).then(response => {
                console.log('Signup Successful: ', response.data);
                form.reset();
                setImageURL(null);
                setError('');
                setFormErrors({})
                window.location.href = "/login";
            }).catch(error => {
                console.log('Signup Error: ', error.response?.data || error.message);
                setError(error.response?.data || error.message);
            });
        }
    };

    const handleLanguageChange = (language) => {
        i18n.changeLanguage(language);
        localStorage.setItem('language', language); // Save the selected language to local storage
    };

    return (
        <ThemeProvider theme={defaultTheme}>
            <Container component="main" maxWidth="xs" sx={{ backgroundColor: 'background.default', minHeight: '100vh' }}>
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
                        {t('sign up')}
                    </Typography>
                    <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3 }} ref={formRef}>
                        <Grid container spacing={2}>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    autoComplete="given-name"
                                    name="firstName"
                                    required
                                    fullWidth
                                    id="firstName"
                                    label={t('first name')}
                                    autoFocus
                                    error={!!formErrors.firstName}
                                    helperText={formErrors.firstName}
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    required
                                    fullWidth
                                    id="lastName"
                                    label={t('last name')}
                                    name="lastName"
                                    autoComplete="family-name"
                                    error={!!formErrors.lastName}
                                    helperText={formErrors.lastName}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    required
                                    fullWidth
                                    id="email"
                                    label={t('email address')}
                                    name="emailAddress"
                                    autoComplete="email"
                                    error={!!formErrors.emailAddress}
                                    helperText={formErrors.emailAddress}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    required
                                    fullWidth
                                    name="password"
                                    label={t('password')}
                                    type="password"
                                    id="password"
                                    autoComplete="new-password"
                                    error={!!formErrors.password}
                                    helperText={formErrors.password}
                                    onChange={handleChange}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <Button
                                    variant="contained"
                                    component="label"
                                    fullWidth
                                >
                                    {t('upload image')}
                                    <input
                                        type="file"
                                        hidden
                                        name="image"
                                        onChange={handleChange}
                                    />
                                </Button>
                                {imageURL && (
                                    <Box mt={2} display="flex" justifyContent="center">
                                        <img src={imageURL} alt="Uploaded" style={{ maxHeight: '200px' }} />
                                    </Box>
                                )}
                            </Grid>
                            <Grid item xs={12}>
                                <FormControlLabel
                                    control={<Checkbox value="allowExtraEmails" color="primary" />}
                                    label={t('i want to receive the latest in education, marketing promotions and updates via email')}
                                />
                            </Grid>
                        </Grid>
                        {error && (
                            <Typography color="error" variant="body2" align="center" sx={{ mt: 2 }}>
                                {error}
                            </Typography>
                        )}
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            sx={{ mt: 3, mb: 2 }}
                        >
                            {t('sign up')}
                        </Button>
                        <Grid container justifyContent="flex-end">
                            <Grid item>
                                <Link href="/login" variant="body2">
                                    {t('already have an account? sign in')}
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
            <Footer/>
        </ThemeProvider>
    );
}
