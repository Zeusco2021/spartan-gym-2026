import { useState, useMemo, useCallback } from 'react';
import {
  Box, Typography, ToggleButton, ToggleButtonGroup,
  CircularProgress, Alert, IconButton, Chip, Paper,
} from '@mui/material';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { useTranslation } from 'react-i18next';
import { useGetEventsQuery, useUpdateEventMutation } from '@/api/calendarApi';
import type { CalendarEvent } from '@/types';

type ViewMode = 'daily' | 'weekly' | 'monthly';
const EC: Record<string, string> = {
  workout: '#4caf50', class: '#2196f3', trainer_session: '#ff9800',
  nutrition_reminder: '#9c27b0', custom: '#607d8b',
};
function weekDates(d: Date) {
  const s = new Date(d); s.setDate(s.getDate() - s.getDay());
  return Array.from({ length: 7 }, (_, i) => { const x = new Date(s); x.setDate(x.getDate() + i); return x; });
}
function monthDates(d: Date) {
  const y = d.getFullYear(), m = d.getMonth(), off = new Date(y, m, 1).getDay();
  return Array.from({ length: 42 }, (_, i) => new Date(y, m, 1 + i - off));
}
function dk(d: Date) { return d.toISOString().split('T')[0]; }

export default function CalendarPage() {
  const { t } = useTranslation('calendar');
  const [view, setView] = useState<ViewMode>('weekly');
  const [cur, setCur] = useState(new Date());
  const [upd] = useUpdateEventMutation();
  const [dragged, setDragged] = useState<CalendarEvent | null>(null);

  const range = useMemo(() => {
    const f = new Date(cur), to = new Date(cur);
    if (view === 'daily') { f.setHours(0,0,0,0); to.setHours(23,59,59,999); }
    else if (view === 'weekly') { f.setDate(f.getDate()-f.getDay()); f.setHours(0,0,0,0); to.setDate(f.getDate()+6); to.setHours(23,59,59,999); }
    else { f.setDate(1); f.setHours(0,0,0,0); to.setMonth(to.getMonth()+1,0); to.setHours(23,59,59,999); }
    return { from: f.toISOString(), to: to.toISOString() };
  }, [cur, view]);
  const { data: events, isLoading, isError } = useGetEventsQuery(range);
  const byDate = useMemo(() => {
    const m: Record<string, CalendarEvent[]> = {};
    events?.forEach(e => { const k = dk(new Date(e.startsAt)); if (!m[k]) m[k] = []; m[k].push(e); });
    return m;
  }, [events]);
  const nav = (dir: number) => {
    const n = new Date(cur);
    if (view === 'daily') n.setDate(n.getDate()+dir);
    else if (view === 'weekly') n.setDate(n.getDate()+7*dir);
    else n.setMonth(n.getMonth()+dir);
    setCur(n);
  };
  const onDragStart = useCallback((ev: CalendarEvent) => (e: React.DragEvent) => {
    setDragged(ev); e.dataTransfer.effectAllowed = 'move'; e.dataTransfer.setData('text/plain', ev.id);
  }, []);
  const onDrop = useCallback((td: Date) => async (e: React.DragEvent) => {
    e.preventDefault(); if (!dragged) return;
    const os = new Date(dragged.startsAt), oe = new Date(dragged.endsAt), dur = oe.getTime()-os.getTime();
    const ns = new Date(td); ns.setHours(os.getHours(), os.getMinutes());
    await upd({ id: dragged.id, data: { startsAt: ns.toISOString(), endsAt: new Date(ns.getTime()+dur).toISOString() } });
    setDragged(null);
  }, [dragged, upd]);
  const onDragOver = (e: React.DragEvent) => { e.preventDefault(); e.dataTransfer.dropEffect = 'move'; };
  const chip = (ev: CalendarEvent) => (
    <Chip key={ev.id} label={ev.title} size="small" draggable onDragStart={onDragStart(ev)}
      sx={{ bgcolor: EC[ev.eventType] ?? EC.custom, color: 'white', mb: 0.5, cursor: 'grab', maxWidth: '100%' }}
      aria-label={`${t('event')} ${ev.title} - ${t(ev.eventType)}`} />
  );
  const cell = (d: Date, cm = true) => {
    const k = dk(d), evs = byDate[k] ?? [], today = dk(d) === dk(new Date());
    return (
      <Paper key={k} variant="outlined" onDrop={onDrop(d)} onDragOver={onDragOver}
        sx={{ p: 1, minHeight: view === 'monthly' ? 80 : 120, opacity: cm ? 1 : 0.4, bgcolor: today ? 'action.selected' : 'background.paper' }}
        aria-label={`${t('day')} ${d.toLocaleDateString()}`}>
        <Typography variant="caption" sx={{ fontWeight: today ? 'bold' : 'normal' }}>{d.getDate()}</Typography>
        <Box sx={{ display: 'flex', flexDirection: 'column', mt: 0.5 }}>{evs.map(chip)}</Box>
      </Paper>
    );
  };

  const daily = () => {
    const evs = byDate[dk(cur)] ?? [];
    return (<Box>
      <Typography variant="h6" sx={{ mb: 2 }}>{cur.toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</Typography>
      {evs.length === 0 && <Alert severity="info">{t('noEvents')}</Alert>}
      {evs.map(ev => (
        <Paper key={ev.id} draggable onDragStart={onDragStart(ev)} sx={{ p: 2, mb: 1, cursor: 'grab', borderLeft: 4, borderColor: EC[ev.eventType] }} aria-label={`${t('event')} ${ev.title}`}>
          <Typography variant="subtitle1">{ev.title}</Typography>
          <Typography variant="body2" color="text.secondary">{new Date(ev.startsAt).toLocaleTimeString()} – {new Date(ev.endsAt).toLocaleTimeString()}</Typography>
          <Chip label={t(ev.eventType)} size="small" sx={{ mt: 0.5 }} />
        </Paper>
      ))}
    </Box>);
  };
  const weekly = () => (<Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 0.5 }}>
    {['Sun','Mon','Tue','Wed','Thu','Fri','Sat'].map(d => <Typography key={d} variant="caption" align="center" sx={{ fontWeight: 'bold', py: 0.5 }}>{d}</Typography>)}
    {weekDates(cur).map(d => cell(d))}
  </Box>);
  const monthly = () => (<Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 0.5 }}>
    {['Sun','Mon','Tue','Wed','Thu','Fri','Sat'].map(d => <Typography key={d} variant="caption" align="center" sx={{ fontWeight: 'bold', py: 0.5 }}>{d}</Typography>)}
    {monthDates(cur).map(d => cell(d, d.getMonth() === cur.getMonth()))}
  </Box>);
  return (
    <Box p={3}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">{t('calendarTitle')}</Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <IconButton onClick={() => nav(-1)} aria-label={t('previous')}><ChevronLeftIcon /></IconButton>
          <Typography variant="subtitle1">{cur.toLocaleDateString(undefined, { month: 'long', year: 'numeric' })}</Typography>
          <IconButton onClick={() => nav(1)} aria-label={t('next')}><ChevronRightIcon /></IconButton>
        </Box>
        <ToggleButtonGroup value={view} exclusive onChange={(_, v) => v && setView(v)} size="small" aria-label={t('viewMode')}>
          <ToggleButton value="daily" aria-label={t('daily')}>{t('daily')}</ToggleButton>
          <ToggleButton value="weekly" aria-label={t('weekly')}>{t('weekly')}</ToggleButton>
          <ToggleButton value="monthly" aria-label={t('monthly')}>{t('monthly')}</ToggleButton>
        </ToggleButtonGroup>
      </Box>
      {isLoading && <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}><CircularProgress aria-label={t('loading')} /></Box>}
      {isError && <Alert severity="error">{t('error')}</Alert>}
      {!isLoading && !isError && <>{view === 'daily' && daily()}{view === 'weekly' && weekly()}{view === 'monthly' && monthly()}</>}
    </Box>
  );
}
