import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { useAppSelector } from '@/app/hooks';
import { lightTheme, darkTheme } from '@/theme/muiTheme';

function App() {
  const themeMode = useAppSelector((state) => state.ui.theme);
  const theme = themeMode === 'dark' ? darkTheme : lightTheme;

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <div>
        <h1>Spartan Golden Gym</h1>
      </div>
    </ThemeProvider>
  );
}

export default App;
