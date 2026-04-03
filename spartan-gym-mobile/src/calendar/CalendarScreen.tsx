import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export const CalendarScreen: React.FC = () => (
  <View style={styles.container}>
    <Text style={styles.title}>Calendario</Text>
  </View>
);

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 24, fontWeight: 'bold' },
});
