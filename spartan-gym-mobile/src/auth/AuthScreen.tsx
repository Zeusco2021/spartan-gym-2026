import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export const AuthScreen: React.FC = () => (
  <View style={styles.container}>
    <Text style={styles.title}>Spartan Golden Gym</Text>
    <Text>Login / Registro</Text>
  </View>
);

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 16 },
});
