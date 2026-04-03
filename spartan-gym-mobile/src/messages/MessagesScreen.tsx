import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export const MessagesScreen: React.FC = () => (
  <View style={styles.container}>
    <Text style={styles.title}>Mensajes</Text>
  </View>
);

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 24, fontWeight: 'bold' },
});
