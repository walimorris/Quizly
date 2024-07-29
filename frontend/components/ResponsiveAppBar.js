import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import Menu from '@mui/material/Menu';
import MenuIcon from '@mui/icons-material/Menu';
import Container from '@mui/material/Container';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Tooltip from '@mui/material/Tooltip';
import MenuItem from '@mui/material/MenuItem';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import i18n from '../src/i18n';
import {useState} from "react";
import ProfileModal from "./ProfileModal";

const ResponsiveAppBar = ({ userDetails, handleLogout }) => {
    const { t } = useTranslation();
    const [anchorElNav, setAnchorElNav] = React.useState(null);
    const [anchorElUser, setAnchorElUser] = React.useState(null);
    const [anchorElLang, setAnchorElLang] = React.useState(null);
    const [modalOpen, setModalOpen] = useState(false);
    const navigate = useNavigate();

    // Load the language from local storage when the component mounts
    React.useEffect(() => {
        const savedLanguage = localStorage.getItem('language');
        if (savedLanguage) {
            i18n.changeLanguage(savedLanguage);
        }
    }, []);

    const handleOpenNavMenu = (event) => {
        setAnchorElNav(event.currentTarget);
    };
    const handleOpenUserMenu = (event) => {
        setAnchorElUser(event.currentTarget);
    };
    const handleOpenLangMenu = (event) => {
        setAnchorElLang(event.currentTarget);
    };

    const handleCloseNavMenu = () => {
        setAnchorElNav(null);
    };
    const handleCloseUserMenu = () => {
        setAnchorElUser(null);
    };
    const handleCloseLangMenu = () => {
        setAnchorElLang(null);
    };

    const handleMenuItemClick = (setting) => {
        if (setting === t('logout')) {
            handleLogout();
        } else if (setting === t('profile')) {
            setModalOpen(true);
        }
    };

    const handleClose = () => {
        setModalOpen(false);
    };

    const handlePageClick = (page) => {
        handleCloseNavMenu();
        if (page === t('documents')) {
            navigate('/documents');
        } else if (page === t('dashboard')) {
            navigate('/dashboard');
        } else if (page === t('blog')) {
            navigate('/blog');
        }
    };

    const handleLanguageChange = (language) => {
        i18n.changeLanguage(language);
        localStorage.setItem('language', language); // Save the selected language to local storage
        handleCloseLangMenu();
    };

    const pages = [t('dashboard'), t('documents'), t('blog')];
    const settings = [t('profile'), t('logout')];

    return (
        <AppBar position="static">
            <Container maxWidth="xl">
                <Toolbar disableGutters>
                    <img
                        src="/images/quizly-logo.png"
                        alt="Quizly Logo"
                        style={{ display: { xs: 'none', md: 'flex' }, marginRight: '16px', height: '40px' }}
                    />

                    <Box sx={{ flexGrow: 1, display: { xs: 'flex', md: 'none' } }}>
                        <IconButton
                            size="large"
                            aria-label="account of current user"
                            aria-controls="menu-appbar"
                            aria-haspopup="true"
                            onClick={handleOpenNavMenu}
                            color="inherit"
                        >
                            <MenuIcon />
                        </IconButton>
                        <Menu
                            id="menu-appbar"
                            anchorEl={anchorElNav}
                            anchorOrigin={{
                                vertical: 'bottom',
                                horizontal: 'left',
                            }}
                            keepMounted
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'left',
                            }}
                            open={Boolean(anchorElNav)}
                            onClose={handleCloseNavMenu}
                            sx={{
                                display: { xs: 'block', md: 'none' },
                            }}
                        >
                            {pages.map((page) => (
                                <MenuItem key={page} onClick={() => handlePageClick(page)}>
                                    <Typography textAlign="center">{page}</Typography>
                                </MenuItem>
                            ))}
                        </Menu>
                    </Box>
                    <Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
                        {pages.map((page) => (
                            <Button
                                key={page}
                                onClick={() => handlePageClick(page)}
                                sx={{ my: 2, color: 'white', display: 'block' }}
                            >
                                {page}
                            </Button>
                        ))}
                    </Box>

                    <Box sx={{ flexGrow: 0, display: 'flex', alignItems: 'center' }}>
                        <Tooltip title="Change language">
                            <Button color="inherit" onClick={handleOpenLangMenu}>
                                {i18n.language.toUpperCase()}
                            </Button>
                        </Tooltip>
                        <Menu
                            sx={{ mt: '45px' }}
                            id="menu-language"
                            anchorEl={anchorElLang}
                            anchorOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            keepMounted
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            open={Boolean(anchorElLang)}
                            onClose={handleCloseLangMenu}
                        >
                            <MenuItem onClick={() => { handleLanguageChange('en'); handleCloseLangMenu(); }}>
                                <img src="/images/flag-us.png" alt="US Flag" style={{ width: 20, height: 10, marginRight: 8 }} />
                                <Typography textAlign="center">English</Typography>
                            </MenuItem>
                            <MenuItem onClick={() => { handleLanguageChange('bg'); handleCloseLangMenu(); }}>
                                <img src="/images/flag-bg.png" alt="BG Flag" style={{ width: 20, height: 10, marginRight: 8 }} />
                                <Typography textAlign="center">Български</Typography>
                            </MenuItem>
                        </Menu>
                        <Tooltip title="Open settings">
                            <IconButton onClick={handleOpenUserMenu} sx={{ p: 0, ml: 2 }}>
                                <Avatar alt={userDetails.firstName} src={userDetails.image} />
                            </IconButton>
                        </Tooltip>
                        <Menu
                            sx={{ mt: '45px' }}
                            id="menu-appbar"
                            anchorEl={anchorElUser}
                            anchorOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            keepMounted
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            open={Boolean(anchorElUser)}
                            onClose={handleCloseUserMenu}
                        >
                            {settings.map((setting) => (
                                <MenuItem key={setting} onClick={() => handleMenuItemClick(setting)}>
                                    <Typography textAlign="center">{setting}</Typography>
                                </MenuItem>
                            ))}
                        </Menu>
                    </Box>
                </Toolbar>
                <ProfileModal open={modalOpen} handleClose={handleClose} />
            </Container>
        </AppBar>
    );
};

export default ResponsiveAppBar;
