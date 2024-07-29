import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import en from '../static/locales/en/translation.json'
import bg from '../static/locales/bg/translation.json';

i18n
    .use(initReactI18next) // Passes i18n down to react-i18next
    .init({
        resources: {
            en: {
                translation: en,
            },
            bg: {
                translation: bg,
            },
        },
        lng: 'en', // Default language
        fallbackLng: 'en', // Fallback language
        interpolation: {
            escapeValue: false, // React already safes from xss
        },
    });

export default i18n;
