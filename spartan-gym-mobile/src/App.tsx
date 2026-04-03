/**
 * Spartan Golden Gym — React Native App Entry Point
 */
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

const App: React.FC = () => (
  <View style={styles.container}>
    <Text style={styles.title}>Spartan Golden Gym</Text>
  </View>
);

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 28, fontWeight: 'bold' },
});

export default App;
