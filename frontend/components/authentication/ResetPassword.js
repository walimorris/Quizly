import React, {useState, useEffect, useRef} from 'react';
import { useLocation } from 'react-router-dom';
import {createTheme, ThemeProvider} from "@mui/material/styles";
import Container from "@mui/material/Container";
import CssBaseline from "@mui/material/CssBaseline";
import Box from "@mui/material/Box";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import Avatar from "@mui/material/Avatar";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";

const defaultTheme = createTheme({
    palette: {
        background: {
            default: "#f8f8f8"
        }
    }
});

// TODO: provide time warning to user somewhere in the process (email, page, etc)
const PasswordReset = () => {
    const formRef = useRef(null);
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [passwordChanged, setPasswordChanged] = useState(false);

    const [validToken, setValidToken] = useState(false);
    const [sessionToken, setSessionToken] = useState('');
    const [error, setError] = useState('');
    const location = useLocation();

    useEffect(() => {
        const query = new URLSearchParams(location.search);
        const sessionToken = query.get('sessionToken');

        if (!sessionToken) {
            setError('Invalid session token');
            return;
        }

        // Validate the session token
        fetch(`/api/auth/validate-onetime-session-token?token=${sessionToken}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Invalid or expired session token');
                }
                return response.json();
            })
            .then(data => {
                setValidToken(true);
                setSessionToken(sessionToken);
            })
            .catch(err => {
                setError(err.message);
            });
    }, [location]);

    if (error) {
        return <div>{error}</div>;
    }

    if (!validToken) {
        return <div>Validating session...</div>;
    }

    const handlePasswordChange = (e) => {
        setPassword(e.target.value);
        setPasswordError('');
    };

    const handleConfirmPasswordChange = (e) => {
        setConfirmPassword(e.target.value);
        setPasswordError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const form = formRef.current;

        if (password !== confirmPassword) {
            setPasswordError('Passwords do not match');
            return;
        }
        try {
            const response = await fetch('/api/auth/password-reset/change-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({password: password, confirmPassword: confirmPassword, token: sessionToken }),
            });
            if (response.ok) {
                setPasswordChanged(true);
                form.reset();
                window.location.href = "/login";
            } else {
                setError('Password Change Failure.');
            }
        } catch (error) {
            console.error('Error changing password:', error);
            setError('Error changing password');
        }
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
                        Password Reset
                    </Typography>
                    <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3 }} ref={formRef}>
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <TextField
                                    required
                                    fullWidth
                                    id="password"
                                    label="New Password"
                                    name="password"
                                    type="password"
                                    value={password}
                                    onChange={handlePasswordChange}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    required
                                    fullWidth
                                    id="confirm-password"
                                    label="Confirm Password"
                                    name="confirm-password"
                                    type="password"
                                    value={confirmPassword}
                                    onChange={handleConfirmPasswordChange}
                                />
                            </Grid>
                            <Button
                                type="submit"
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2 }}
                            >
                                Reset Password
                            </Button>
                            {passwordError && (
                                <Typography color="error" variant="body2" align="center" sx={{ mt: 1 }}>
                                    {passwordError}
                                </Typography>
                            )}
                        </Grid>
                    </Box>
                </Box>
            </Container>
        </ThemeProvider>
    );
};

export default PasswordReset;
