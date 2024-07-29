import axios from 'axios';

export const validateToken = async (token) => {
    try {
        const response = await axios.get('/api/auth/validate_token', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data === true;
    } catch (error) {
        console.error('Token validation error:', error);
        return false;
    }
};

export const getUserDetails = () => {
    const userDetails = localStorage.getItem('userDetails');
    return userDetails ? JSON.parse(userDetails) : null;
};