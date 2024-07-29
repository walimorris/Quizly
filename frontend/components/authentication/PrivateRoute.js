import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { validateToken } from '../../utils/auth';

const PrivateRoute = ({ children }) => {
    const [isValid, setIsValid] = useState(null);

    useEffect(() => {
        const checkToken = async () => {
            const token = localStorage.getItem('accessToken');
            if (token) {
                const valid = await validateToken(token);
                setIsValid(valid);
            } else {
                setIsValid(false);
                console.log("not valid");
            }
        };
        checkToken();
    }, []);

    if (isValid === null) {
        return <div>Loading...</div>; // Or any loading indicator
    }

    return isValid ? children : <Navigate to="/login" replace />;
};

export default PrivateRoute;

