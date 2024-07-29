import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { quantum } from "ldrs";
import { getUserDetails } from '../utils/auth';
import { useTranslation } from 'react-i18next';
import Footer from "./Footer";
import Modal from "@mui/material/Modal";
import {Alert, Backdrop, Fade} from "@mui/material";
import IconButton from "@mui/material/IconButton";
import CloseIcon from '@mui/icons-material/Close';
import CancelIcon from '@mui/icons-material/Cancel';
import SendIcon from '@mui/icons-material/Send';
import Stack from "@mui/material/Stack";
import Avatar from "@mui/material/Avatar";

const defaultTheme = createTheme({
    palette: {
        background: {
            default: "#f8f8f8" // off-white with a slight gray tint
        }
    }
});

const ProfileModal = ({  changeLanguage, open, handleClose }) => {
    const { t, i18n } = useTranslation();

    const [showCradle, setShowCradle] = useState(false);
    const [userDetails, setUserDetails] = useState(getUserDetails());
    const [submitOnNoUpdates, setSubmitOnNoUpdates] = useState(false);

    const [username, setUserName] = useState(userDetails['emailAddress']);
    const [name, setName] = useState(userDetails['firstName']);

    useEffect(() => {
        const savedLanguage = localStorage.getItem('language');
        if (savedLanguage) {
            i18n.changeLanguage(savedLanguage);
        }
    }, []);

    const settingsModalCloseButton = {
        left: '90%'
    }

    const settingsModalInputField = {
        marginRight: '20%'
    }

    /**
     * Any updated values should be set to its original value in
     * the case of cancel closures on the profile settings view.
     */
    const handleCancelClose = () => {
        handleClose();
    }

    async function handleUpdateSubmit(e) {
        e.preventDefault();
    }

    // handle user detail property updates
    const handleEmailChange = (e) => setUserName(e.target.value);
    const handleNameChange = (e) => setName(e.target.value);

    quantum.register();

    const style = {
        position: 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: '50%',
        bgcolor: 'background.paper',
        boxShadow: 24,
        border: '2px solid #000',
        borderRadius: '12px',
        p: 4,
    };

    return (
        <ThemeProvider theme={defaultTheme}>
            <Modal
                aria-labelledby="transition-modal-title"
                aria-describedby="transition-modal-description"
                open={open}
                onClose={handleClose}
                closeAfterTransition
                slots={{ backdrop: Backdrop }}
                slotProps={{
                    backdrop: {
                        timeout: 500,
                    },
                }}
            >
                <Fade in={open}>
                    <Box sx={style}>
                        <IconButton sx={settingsModalCloseButton} color="inherit" onClick={handleCancelClose}>
                            <CloseIcon
                                fontSize="large"
                                color="action"
                                right="50%"
                            />
                        </IconButton>
                        <Box
                            component="form"
                            sx={{'& .MuiTextField-root': { m: 2, width: '100%' },}}
                            noValidate
                            autoComplete="off"
                        >
                            <div>
                                <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '20px' }}>
                                    <Avatar
                                        alt="User Profile Picture"
                                        src={userDetails.image}
                                        sx={{ width: 120, height: 120 }}
                                    />
                                </div>
                                <Alert
                                    sx={{ width: '75%', right: '50%', marginLeft: '12%', marginTop: '2%', marginBottom: '2%' }}
                                    severity="info">
                                    {t('make-quizly-better')} <a href="#">ai.quizly@gmail.com</a>
                                </Alert>
                            </div>
                            <div>
                                <TextField
                                    fullWidth
                                    size={"small"}
                                    sx={settingsModalInputField}
                                    id="outlined-search"
                                    value={name}
                                    onChange={handleNameChange}
                                    label={t("name")}
                                    style={{width: '75%', marginLeft: '12%'}}
                                    type="text" />
                            </div>
                            <div>
                                <TextField
                                    fullWidth
                                    size={"small"}
                                    sx={settingsModalInputField}
                                    id="outlined-search"
                                    value={username}
                                    onChange={handleEmailChange}
                                    label={t("company/personal email")}
                                    style={{width: '75%', marginLeft: '12%'}}
                                    type="text" />
                            </div>
                            <div>
                                { submitOnNoUpdates && <Alert
                                    sx={{ width: '75%', right: '50%', marginLeft: '12%', marginTop: '1%', marginBottom: '1%' }}
                                    severity="warning">
                                    {t('no-user-updates')}
                                </Alert> }
                            </div>
                            <div>
                                <Stack direction="row" justifyContent="space-between" sx={{ marginTop: '3%', width: '50%', marginLeft: '25%' }}>
                                    <Button
                                        variant="outlined"
                                        onClick={handleCancelClose}
                                        startIcon={<CancelIcon />}
                                        sx={{ flex: 1, marginRight: '8px' }}
                                    >
                                        {t('cancel')}
                                    </Button>
                                    <Button
                                        onClick={handleUpdateSubmit}
                                        variant="contained"
                                        endIcon={<SendIcon />}
                                        sx={{ flex: 1, marginLeft: '8px' }}
                                    >
                                        {t('update')}
                                    </Button>
                                </Stack>
                            </div>
                            <Footer/>
                        </Box>
                    </Box>
                </Fade>
            </Modal>
        </ThemeProvider>
    )
}

export default ProfileModal;