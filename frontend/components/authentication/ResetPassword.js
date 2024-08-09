import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';

const PasswordReset = () => {
    const [validToken, setValidToken] = useState(false);
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

    return (
        <div>
            {/* Password reset form */}
            <h2>Reset your password</h2>
            <form>
                {/* Form fields */}
                <input type="password" placeholder="New Password" />
                <button type="submit">Reset Password</button>
            </form>
        </div>
    );
};

export default PasswordReset;
