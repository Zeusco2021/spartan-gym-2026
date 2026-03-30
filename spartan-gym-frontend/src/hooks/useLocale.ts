import { useTranslation } from 'react-i18next';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { setLocale } from '@/features/settings/uiSlice';

export function useLocale() {
  const { locale, measurementUnit } = useAppSelector((state) => state.ui);
  const dispatch = useAppDispatch();
  const { i18n } = useTranslation();

  const changeLocale = (newLocale: string) => {
    i18n.changeLanguage(newLocale);
    dispatch(setLocale(newLocale));
  };

  const formatDate = (date: string) =>
    new Intl.DateTimeFormat(locale).format(new Date(date));

  const formatCurrency = (amount: number, currency: string) =>
    new Intl.NumberFormat(locale, { style: 'currency', currency }).format(amount);

  const formatWeight = (kg: number) =>
    measurementUnit === 'imperial'
      ? `${(kg * 2.20462).toFixed(1)} lbs`
      : `${kg} kg`;

  const formatDistance = (km: number) =>
    measurementUnit === 'imperial'
      ? `${(km * 0.621371).toFixed(1)} mi`
      : `${km} km`;

  return { locale, changeLocale, formatDate, formatCurrency, formatWeight, formatDistance };
}
