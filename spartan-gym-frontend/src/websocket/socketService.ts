import { io, type Socket } from 'socket.io-client';
import type { AppDispatch } from '@/app/store';
import type { Message } from '@/types/messages';
import {
  setConnected,
  addIncomingMessage,
  updateOccupancy,
  setTypingUsers,
  updateRanking,
  updateLiveWorkout,
} from '@/features/websocket/websocketSlice';
import { messagesApi } from '@/api/messagesApi';
import { notificationsApi } from '@/api/notificationsApi';

class SocketService {
  private socket: Socket | null = null;
  private dispatch: AppDispatch | null = null;

  connect(token: string, dispatch: AppDispatch): void {
    this.dispatch = dispatch;
    this.socket = io(import.meta.env.VITE_WS_URL || 'http://localhost:8092', {
      auth: { token },
      transports: ['websocket'],
      reconnection: true,
      reconnectionAttempts: 10,
      reconnectionDelay: 1000,
    });

    this.socket.on('connect', () => dispatch(setConnected(true)));
    this.socket.on('disconnect', () => dispatch(setConnected(false)));

    // Chat
    this.socket.on('message:new', (message: Message) => {
      dispatch(addIncomingMessage(message));
      dispatch(
        messagesApi.util.invalidateTags([
          { type: 'Message', id: message.conversationId },
          'Conversation',
        ]),
      );
    });

    this.socket.on(
      'message:read',
      ({
        messageId: _messageId,
        conversationId,
      }: {
        messageId: string;
        conversationId: string;
      }) => {
        dispatch(
          messagesApi.util.invalidateTags([
            { type: 'Message', id: conversationId },
          ]),
        );
      },
    );

    this.socket.on(
      'message:typing',
      ({
        conversationId,
        userId,
      }: {
        conversationId: string;
        userId: string;
      }) => {
        dispatch(setTypingUsers({ conversationId, users: [userId] }));
      },
    );

    // Gym occupancy
    this.socket.on(
      'gym:occupancy',
      ({ gymId, occupancy }: { gymId: string; occupancy: number }) => {
        dispatch(updateOccupancy({ gymId, occupancy }));
      },
    );

    // Rankings
    this.socket.on(
      'ranking:update',
      (data: { category: string; entries: Array<{ userId: string; score: number; rank: number }> }) => {
        dispatch(updateRanking(data));
      },
    );

    // Live workout sync
    this.socket.on(
      'workout:live',
      (data: { userId: string; sessionId: string; currentExercise: string; progress: number }) => {
        dispatch(updateLiveWorkout(data));
      },
    );

    // Real-time notifications
    this.socket.on('notification:new', () => {
      dispatch(notificationsApi.util.invalidateTags(['Notification']));
    });
  }

  sendMessage(
    conversationId: string,
    content: string,
    contentType = 'text',
  ): void {
    this.socket?.emit('message:send', { conversationId, content, contentType });
  }

  markAsRead(messageId: string, conversationId: string): void {
    this.socket?.emit('message:read', { messageId, conversationId });
  }

  startTyping(conversationId: string): void {
    this.socket?.emit('message:typing', { conversationId });
  }

  joinGymOccupancy(gymId: string): void {
    this.socket?.emit('gym:subscribe', { gymId });
  }

  leaveGymOccupancy(gymId: string): void {
    this.socket?.emit('gym:unsubscribe', { gymId });
  }

  joinRanking(category: string): void {
    this.socket?.emit('ranking:subscribe', { category });
  }

  leaveRanking(category: string): void {
    this.socket?.emit('ranking:unsubscribe', { category });
  }

  joinLiveWorkout(sessionId: string): void {
    this.socket?.emit('workout:live:join', { sessionId });
  }

  leaveLiveWorkout(sessionId: string): void {
    this.socket?.emit('workout:live:leave', { sessionId });
  }

  disconnect(): void {
    this.socket?.disconnect();
    this.socket = null;
  }
}

export const socketService = new SocketService();
