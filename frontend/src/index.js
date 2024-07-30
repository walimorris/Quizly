import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './app';
import i18n from "i18next";
import {I18nextProvider} from "react-i18next";

const container = document.getElementById('root');
const root = createRoot(container);


root.render(
    <I18nextProvider i18n={i18n}>
        <App />
    </I18nextProvider>
);

