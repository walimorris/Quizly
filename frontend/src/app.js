import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Login from '../components/authentication/Login';
import Signup from '../components/authentication/Signup';
import ForgotPassword from "../components/authentication/ForgotPassword";
import Dashboard from '../components/Dashboard';
import PrivateRoute from '../components/authentication/PrivateRoute';
import { validateToken } from '../utils/auth';
import Documents from "../components/Documents";
import {I18nextProvider} from "react-i18next";
import i18n from "./i18n";
import Blog from "../components/blog/Blog";
import Grid from "@mui/material/Grid";
import Box from "@mui/material/Box";
import ResetPassword from "../components/authentication/ResetPassword";

const App = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(null);

    useEffect(() => {
        const checkAuth = async () => {
            const token = localStorage.getItem('accessToken');
            if (token) {
                const valid = await validateToken(token);
                setIsAuthenticated(valid);
            } else {
                setIsAuthenticated(false);
            }
        };
        checkAuth();
    }, []);

    if (isAuthenticated === null) {
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
        <I18nextProvider i18n={i18n}>
            <Router>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/signup" element={<Signup />} />
                    <Route path="/forgot-password" element={<ForgotPassword />} />
                    <Route path="/reset-password" element={<ResetPassword />} />
                    <Route path="/dashboard" element={
                        <PrivateRoute>
                            <Dashboard />
                        </PrivateRoute>
                    } />
                    <Route path="/documents" element={
                        <PrivateRoute>
                            <Documents />
                        </PrivateRoute>
                    } />
                    <Route path="/blog" element={
                        <PrivateRoute>
                            <Blog />
                        </PrivateRoute>
                    } />
                    <Route path="/" element={
                        isAuthenticated ? <Navigate to="/dashboard" replace /> : <Navigate to="/login" replace />
                    } />
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </Router>
        </I18nextProvider>
    );
};

export default App;

