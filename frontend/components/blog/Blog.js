import * as React from 'react';
import CssBaseline from '@mui/material/CssBaseline';
import Grid from '@mui/material/Grid';
import Container from '@mui/material/Container';
import GitHubIcon from '@mui/icons-material/GitHub';
import FacebookIcon from '@mui/icons-material/Facebook';
import XIcon from '@mui/icons-material/X';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import MainFeaturedPost from './MainFeaturedPost';
import FeaturedPost from './FeaturedPost';
import Main from './Main';
import Sidebar from './Sidebar';
import Footer from '../Footer';
import post1 from '../blog/posts/blog-post.1.md';
import ResponsiveAppBar from "../ResponsiveAppBar";
import {useTranslation} from "react-i18next";
import {getUserDetails} from "../../utils/auth";
import {useEffect} from "react";

const defaultTheme = createTheme({
    palette: {
        background: {
            default: "#f8f8f8"
        }
    }
});

export default function Blog({ changeLanguage }) {
    const { t, i18n } = useTranslation();
    const userDetails = getUserDetails();

    useEffect(() => {
        const savedLanguage = localStorage.getItem('language');
        if (savedLanguage) {
            i18n.changeLanguage(savedLanguage);
        }
    }, [i18n]);

    const mainFeaturedPost = {
        title: 'Introducing Quizly.com: Your Path to Quiz Mastery',
        description:
            "Discover how Quizly.com transforms the quiz-taking experience into an engaging and effective journey with AI-powered quizzes, expert tips, and invaluable resources. Prepare to ace your exams and enhance your learning with us!",
        image: '/images/quizly-logo.png',
        imageText: 'Quizly Logo',
        linkText: t('blog-continue-reading')
    };

    const sidebar = {
        title: t('about'),
        description:t('blog-description'),
        social: [
            { name: 'GitHub', icon: GitHubIcon },
            { name: 'X', icon: XIcon },
            { name: 'Facebook', icon: FacebookIcon },
        ],
    };

    const featuredPosts = [
        {
            title: 'Are We There Yet? Skills-Based Technologies, Hiring and Advancement',
            date: 'July 19, 2024',
            description:
                'SkillRise, an ISTE initiative, explored job seekers\' views on digital skills and skills-based technologies in 2024. The research highlights the crucial need for digital skills training and raises awareness about how these tools can boost career prospects, from hiring to advancement.',
            image: '/images/are-we-there-yet-skills-based-technologies-hiring-and-advancement.png',
            imageLabel: 'Skills-Based Technologies',
            link: 'https://www.edsurge.com/news/2024-07-19-are-we-there-yet-skills-based-technologies-hiring-and-advancement'
        },
        {
            title: 'Finding the Right Technology for Early Elementary Classrooms',
            date: 'June 19, 2024',
            description:
                'Introducing iPads to a bustling Kindergarten classroom felt like the Hunger Games. Amidst the excitement, the real challenge emerged: clunky, confusing apps that frustrated our young learners. Discover the nine key features essential for selecting the right edtech tools for our youngest students.',
            image: '/images/finding-the-right-technology-for-early-elementary-classrooms.png',
            imageLabel: 'students find the right technology',
            link: 'https://www.edsurge.com/news/2024-06-19-finding-the-right-technology-for-early-elementary-classrooms'
        },
    ];

    const posts = [post1];

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userDetails');
        window.location.href = '/login';
    };

    return (
        <ThemeProvider theme={defaultTheme}>
            <ResponsiveAppBar userDetails={userDetails} handleLogout={handleLogout} changeLanguage={changeLanguage} />
            <CssBaseline />
            <Container sx={{ backgroundColor: 'background.default', minHeight: '100vh', position: 'relative', mt: 8 }}>
                <main>
                    <MainFeaturedPost post={mainFeaturedPost} />
                    <Grid container spacing={4}>
                        {featuredPosts.map((post) => (
                            <FeaturedPost key={post.title} post={post} />
                        ))}
                    </Grid>
                    <Grid container spacing={5} sx={{ mt: 3 }}>
                        <Main title={t('blog-title')} posts={posts} />
                        <Sidebar
                            title={sidebar.title}
                            description={sidebar.description}
                            social={sidebar.social}
                        />
                    </Grid>
                </main>
            </Container>
            <Footer/>
        </ThemeProvider>
    );
}
