import { createTheme } from '@mui/material/styles';

const fontFamily = ['Inter', 'Roboto', 'Helvetica', 'Arial', 'sans-serif'].join(',');

export const lightTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#D4AF37',
      light: '#FFD700',
      dark: '#B8960C',
      contrastText: '#FFFFFF',
    },
    background: {
      default: '#F5F5F5',
      paper: '#FFFFFF',
    },
  },
  typography: {
    fontFamily,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 8,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
  },
});

export const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#FFD700',
      light: '#FFE44D',
      dark: '#D4AF37',
      contrastText: '#1A1A1A',
    },
    background: {
      default: '#1A1A1A',
      paper: '#2D2D2D',
    },
  },
  typography: {
    fontFamily,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 8,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
  },
});
