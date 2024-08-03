import React, { useState } from 'react';
import ReCAPTCHA from 'react-google-recaptcha';

export default function ForgotPassword() {
    const [recaptchaValue, setRecaptchaValue] = useState(null);
    const [recaptchaVerified, setRecaptchaVerified] = useState(false);
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');

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
            setMessage('Password reset email sent');
        } else {
            setMessage('Please enter your email');
        }
    };

    return (
        <div>
            {message && <p>{message}</p>}
            <form onSubmit={handleEmailSubmit}>
                <div className="form-group">
                    <label htmlFor="email">Email:</label>
                    <input
                        type="email"
                        id="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>
                {!recaptchaVerified && (
                    <div className="form-group">
                        <ReCAPTCHA
                            sitekey="6LfJcx4qAAAAALCKesk_ETIqiqtLHNY8BufzqdW6"
                            onChange={handleRecaptchaChange}
                            action="password_reset"
                        />
                        <button onClick={handleRecaptchaSubmit}>Verify reCAPTCHA</button>
                    </div>
                )}
                <button type="submit" disabled={!recaptchaVerified}>Send Password Reset Email</button>
            </form>
        </div>
    );
};
