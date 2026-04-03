import { useState, useMemo } from 'react';
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Chip,
  Avatar,
  List,
  ListItemButton,
  ListItemAvatar,
  ListItemText,
  Tabs,
  Tab,
  Paper,
} from '@mui/material';
import {
  VictoryChart,
  VictoryLine,
  VictoryBar,
  VictoryAxis,
  VictoryTheme,
  VictoryTooltip,
} from 'victory';
import { useTranslation } from 'react-i18next';
import { useGetPlansQuery } from '@/api/trainingApi';
import { useGetTrainerClientsQuery, useGetClientProgressQuery } from '@/api/trainerApi';
import type { TrainerClient } from '@/types/trainer';

type ProgressTab = 'volume' | 'duration' | 'calories';

export default function TrainerPanelPage() {
  const { t } = useTranslation('trainer');

  const { data: clientsData, isLoading: clientsLoading, isError: clientsError } =
    useGetTrainerClientsQuery();
  const { data: plansData, isLoading: plansLoading } = useGetPlansQuery({ page: 0, size: 100 });

  const [selectedClientId, setSelectedClientId] = useState<string | null>(null);
  const [progressTab, setProgressTab] = useState<ProgressTab>('volume');

  const clients = clientsData ?? [];
  const plans = plansData?.content ?? [];

  const selectedClient = useMemo(
    () => clients.find((c) => c.id === selectedClientId) ?? null,
    [clients, selectedClientId],
  );

  const clientPlans = useMemo(
    () => plans.filter((p) => p.userId === selectedClientId && p.status === 'active'),
    [plans, selectedClientId],
  );

  const isLoading = clientsLoading || plansLoading;

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('trainerPanel')}
      </Typography>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress aria-label={t('loading')} />
        </Box>
      )}

      {clientsError && <Alert severity="error">{t('loadError')}</Alert>}

      {!isLoading && !clientsError && clients.length === 0 && (
        <Alert severity="info">{t('noClients')}</Alert>
      )}

      {!isLoading && !clientsError && clients.length > 0 && (
        <Box sx={{ display: 'flex', gap: 3, flexDirection: { xs: 'column', md: 'row' } }}>
          {/* Client list */}
          <Paper variant="outlined" sx={{ minWidth: 280, maxHeight: 600, overflow: 'auto' }}>
            <List aria-label={t('clientList')}>
              {clients.map((client) => {
                const activePlanCount = plans.filter(
                  (p) => p.userId === client.id && p.status === 'active',
                ).length;
                return (
                  <ListItemButton
                    key={client.id}
                    selected={selectedClientId === client.id}
                    onClick={() => setSelectedClientId(client.id)}
                    aria-label={`${t('selectClient')}: ${client.name}`}
                  >
                    <ListItemAvatar>
                      <Avatar src={client.profilePhotoUrl} alt={client.name}>
                        {client.name.charAt(0)}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={client.name}
                      secondary={
                        <Chip
                          label={`${activePlanCount} ${t('activePlans')}`}
                          size="small"
                          color={activePlanCount > 0 ? 'primary' : 'default'}
                          variant="outlined"
                        />
                      }
                    />
                  </ListItemButton>
                );
              })}
            </List>
          </Paper>

          {/* Client detail */}
          <Box sx={{ flex: 1 }}>
            {!selectedClient && (
              <Alert severity="info">{t('selectClientPrompt')}</Alert>
            )}

            {selectedClient && (
              <>
                <Typography variant="h5" gutterBottom>
                  {selectedClient.name}
                </Typography>

                {/* Active plans */}
                <Card sx={{ mb: 3 }}>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      {t('activePlans')}
                    </Typography>
                    {clientPlans.length === 0 ? (
                      <Alert severity="info">{t('noActivePlans')}</Alert>
                    ) : (
                      clientPlans.map((plan) => (
                        <Box
                          key={plan.id}
                          sx={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 2,
                            py: 1,
                            borderBottom: 1,
                            borderColor: 'divider',
                          }}
                        >
                          <Typography variant="body1" sx={{ flex: 1 }}>
                            {plan.name}
                          </Typography>
                          <Chip
                            label={plan.aiGenerated ? t('aiGenerated') : t('manual')}
                            size="small"
                            color={plan.aiGenerated ? 'secondary' : 'default'}
                            variant="outlined"
                          />
                          <Typography variant="caption" color="text.secondary">
                            {plan.routines.length} {t('routines')}
                          </Typography>
                        </Box>
                      ))
                    )}
                  </CardContent>
                </Card>

                {/* Progress charts */}
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      {t('progressMetrics')}
                    </Typography>
                    <Tabs
                      value={progressTab}
                      onChange={(_, v) => setProgressTab(v as ProgressTab)}
                      aria-label={t('progressTabs')}
                      sx={{ mb: 2 }}
                    >
                      <Tab value="volume" label={t('volume')} />
                      <Tab value="duration" label={t('duration')} />
                      <Tab value="calories" label={t('calories')} />
                    </Tabs>
                    <ClientProgressChart
                      clientId={selectedClient.id}
                      metric={progressTab}
                    />
                  </CardContent>
                </Card>
              </>
            )}
          </Box>
        </Box>
      )}
    </Box>
  );
}


interface ClientProgressChartProps {
  clientId: string;
  metric: ProgressTab;
}

function ClientProgressChart({ clientId, metric }: ClientProgressChartProps) {
  const { t } = useTranslation('trainer');

  const { data: progress, isLoading, isError } = useGetClientProgressQuery({
    clientId,
    period: 'month',
  });

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress size={24} aria-label={t('loadingProgress')} />
      </Box>
    );
  }

  if (isError || !progress || progress.data.length === 0) {
    return <Alert severity="info">{t('noProgressData')}</Alert>;
  }

  const chartData = progress.data.map((d) => ({
    x: new Date(d.date).toLocaleDateString(undefined, { month: 'short', day: 'numeric' }),
    y: d[metric === 'calories' ? 'caloriesBurned' : metric],
  }));

  const metricLabels: Record<ProgressTab, string> = {
    volume: t('volume'),
    duration: t('duration'),
    calories: t('calories'),
  };

  return (
    <Box aria-label={`${t('progressChart')}: ${metricLabels[metric]}`} role="img">
      <VictoryChart
        theme={VictoryTheme.material}
        height={300}
        padding={{ top: 20, bottom: 50, left: 60, right: 20 }}
      >
        <VictoryAxis
          tickFormat={(tick: string) => tick}
          style={{ tickLabels: { fontSize: 10, angle: -45 } }}
        />
        <VictoryAxis
          dependentAxis
          label={metricLabels[metric]}
          style={{ axisLabel: { padding: 45, fontSize: 12 } }}
        />
        {metric === 'volume' ? (
          <VictoryBar
            data={chartData}
            style={{ data: { fill: '#1976d2' } }}
            labelComponent={<VictoryTooltip />}
            labels={({ datum }) => `${datum.y}`}
          />
        ) : (
          <VictoryLine
            data={chartData}
            style={{ data: { stroke: metric === 'duration' ? '#ff9800' : '#4caf50', strokeWidth: 2 } }}
            labelComponent={<VictoryTooltip />}
            labels={({ datum }) => `${datum.y}`}
          />
        )}
      </VictoryChart>
    </Box>
  );
}
