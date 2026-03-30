import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import HttpBackend from 'i18next-http-backend';

export const supportedLanguages = ['en', 'es', 'fr', 'de', 'ja'] as const;
export type SupportedLanguage = (typeof supportedLanguages)[number];

export const defaultNamespace = 'common';
export const namespaces = [
  'common',
  'auth',
  'dashboard',
  'training',
  'nutrition',
  'social',
  'gym',
  'payments',
  'calendar',
  'messages',
  'trainer',
  'analytics',
  'settings',
] as const;

i18n
  .use(HttpBackend)
  .use(initReactI18next)
  .init({
    supportedLngs: supportedLanguages,
    fallbackLng: 'en',
    defaultNS: defaultNamespace,
    ns: [defaultNamespace],
    backend: {
      loadPath: '/locales/{{lng}}/{{ns}}.json',
    },
    interpolation: {
      escapeValue: false,
    },
    react: {
      useSuspense: true,
    },
  });

export default i18n;
