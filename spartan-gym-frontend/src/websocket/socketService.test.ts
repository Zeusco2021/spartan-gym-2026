import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import type { AppDispatch } from '@/app/store';
import type { Message } from '@/types/messages';

// Mock socket.io-client
const mockOn = vi.fn();
const mockEmit = vi.fn();
const mockDisconnect = vi.fn();

vi.mock('socket.io-client', () => ({
  io: vi.fn(() => ({
    on: mockOn,
    emit: mockEmit,
    disconnect: mockDisconnect,
  })),
}));

// Mock Redux slices
vi.mock('@/features/websocket/websocketSlice', () => ({
  setConnected: vi.fn((val: boolean) => ({
    type: 'websocket/setConnected',
    payload: val,
  })),
  addIncomingMessage: vi.fn((msg: Message) => ({
    type: 'websocket/addIncomingMessage',
    payload: msg,
  })),
  updateOccupancy: vi.fn(
    (data: { gymId: string; occupancy: number }) => ({
      type: 'websocket/updateOccupancy',
      payload: data,
    }),
  ),
  setTypingUsers: vi.fn(
    (data: { conversationId: string; users: string[] }) => ({
      type: 'websocket/setTypingUsers',
      payload: data,
    }),
  ),
}));

vi.mock('@/api/messagesApi', () => ({
  messagesApi: {
    util: {
      invalidateTags: vi.fn((tags: unknown[]) => ({
        type: 'messagesApi/invalidateTags',
        payload: tags,
      })),
    },
  },
}));

vi.mock('@/api/notificationsApi', () => ({
  notificationsApi: {
    util: {
      invalidateTags: vi.fn((tags: unknown[]) => ({
        type: 'notificationsApi/invalidateTags',
        payload: tags,
      })),
    },
  },
}));

import { socketService } from './socketService';
import { io } from 'socket.io-client';
import {
  setConnected,
  addIncomingMessage,
  updateOccupancy,
  setTypingUsers,
} from '@/features/websocket/websocketSlice';
import { messagesApi } from '@/api/messagesApi';
import { notificationsApi } from '@/api/notificationsApi';

describe('SocketService', () => {
  let mockDispatch: AppDispatch;
  const token = 'test-jwt-token';

  // Collect registered event handlers
  let eventHandlers: Record<string, (...args: unknown[]) => void>;

  beforeEach(() => {
    vi.clearAllMocks();
    mockDispatch = vi.fn() as unknown as AppDispatch;
    eventHandlers = {};
    mockOn.mockImplementation(
      (event: string, handler: (...args: unknown[]) => void) => {
        eventHandlers[event] = handler;
      },
    );
  });

  afterEach(() => {
    socketService.disconnect();
  });

  describe('connect', () => {
    it('creates a socket connection with correct options', () => {
      socketService.connect(token, mockDispatch);

      expect(io).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          auth: { token },
          transports: ['websocket'],
          reconnection: true,
          reconnectionAttempts: 10,
          reconnectionDelay: 1000,
        }),
      );
    });

    it('registers all required event handlers', () => {
      socketService.connect(token, mockDispatch);

      expect(eventHandlers).toHaveProperty('connect');
      expect(eventHandlers).toHaveProperty('disconnect');
      expect(eventHandlers).toHaveProperty('message:new');
      expect(eventHandlers).toHaveProperty('message:read');
      expect(eventHandlers).toHaveProperty('message:typing');
      expect(eventHandlers).toHaveProperty('gym:occupancy');
      expect(eventHandlers).toHaveProperty('notification:new');
    });

    it('dispatches setConnected(true) on connect', () => {
      socketService.connect(token, mockDispatch);
      eventHandlers['connect']();

      expect(setConnected).toHaveBeenCalledWith(true);
      expect(mockDispatch).toHaveBeenCalled();
    });

    it('dispatches setConnected(false) on disconnect', () => {
      socketService.connect(token, mockDispatch);
      eventHandlers['disconnect']();

      expect(setConnected).toHaveBeenCalledWith(false);
      expect(mockDispatch).toHaveBeenCalled();
    });
  });

  describe('event handlers', () => {
    const testMessage: Message = {
      id: 'msg-1',
      conversationId: 'conv-1',
      senderId: 'user-1',
      content: 'Hello',
      contentType: 'text',
      status: 'sent',
      sentAt: '2024-01-01T00:00:00Z',
    };

    beforeEach(() => {
      socketService.connect(token, mockDispatch);
    });

    it('handles message:new by dispatching addIncomingMessage and invalidating tags', () => {
      eventHandlers['message:new'](testMessage);

      expect(addIncomingMessage).toHaveBeenCalledWith(testMessage);
      expect(messagesApi.util.invalidateTags).toHaveBeenCalledWith([
        { type: 'Message', id: 'conv-1' },
        'Conversation',
      ]);
    });

    it('handles message:read by invalidating message tags', () => {
      eventHandlers['message:read']({
        messageId: 'msg-1',
        conversationId: 'conv-1',
      });

      expect(messagesApi.util.invalidateTags).toHaveBeenCalledWith([
        { type: 'Message', id: 'conv-1' },
      ]);
    });

    it('handles message:typing by dispatching setTypingUsers', () => {
      eventHandlers['message:typing']({
        conversationId: 'conv-1',
        userId: 'user-2',
      });

      expect(setTypingUsers).toHaveBeenCalledWith({
        conversationId: 'conv-1',
        users: ['user-2'],
      });
    });

    it('handles gym:occupancy by dispatching updateOccupancy', () => {
      eventHandlers['gym:occupancy']({ gymId: 'gym-1', occupancy: 42 });

      expect(updateOccupancy).toHaveBeenCalledWith({
        gymId: 'gym-1',
        occupancy: 42,
      });
    });

    it('handles notification:new by invalidating notification tags', () => {
      eventHandlers['notification:new']({});

      expect(notificationsApi.util.invalidateTags).toHaveBeenCalledWith([
        'Notification',
      ]);
    });
  });

  describe('emit methods', () => {
    beforeEach(() => {
      socketService.connect(token, mockDispatch);
    });

    it('sendMessage emits message:send with correct payload', () => {
      socketService.sendMessage('conv-1', 'Hello', 'text');

      expect(mockEmit).toHaveBeenCalledWith('message:send', {
        conversationId: 'conv-1',
        content: 'Hello',
        contentType: 'text',
      });
    });

    it('sendMessage defaults contentType to text', () => {
      socketService.sendMessage('conv-1', 'Hello');

      expect(mockEmit).toHaveBeenCalledWith('message:send', {
        conversationId: 'conv-1',
        content: 'Hello',
        contentType: 'text',
      });
    });

    it('markAsRead emits message:read with correct payload', () => {
      socketService.markAsRead('msg-1', 'conv-1');

      expect(mockEmit).toHaveBeenCalledWith('message:read', {
        messageId: 'msg-1',
        conversationId: 'conv-1',
      });
    });

    it('startTyping emits message:typing with correct payload', () => {
      socketService.startTyping('conv-1');

      expect(mockEmit).toHaveBeenCalledWith('message:typing', {
        conversationId: 'conv-1',
      });
    });

    it('joinGymOccupancy emits gym:subscribe with correct payload', () => {
      socketService.joinGymOccupancy('gym-1');

      expect(mockEmit).toHaveBeenCalledWith('gym:subscribe', {
        gymId: 'gym-1',
      });
    });

    it('leaveGymOccupancy emits gym:unsubscribe with correct payload', () => {
      socketService.leaveGymOccupancy('gym-1');

      expect(mockEmit).toHaveBeenCalledWith('gym:unsubscribe', {
        gymId: 'gym-1',
      });
    });
  });

  describe('disconnect', () => {
    it('disconnects the socket and nullifies it', () => {
      socketService.connect(token, mockDispatch);
      socketService.disconnect();

      expect(mockDisconnect).toHaveBeenCalled();
    });

    it('does not throw when disconnecting without a connection', () => {
      expect(() => socketService.disconnect()).not.toThrow();
    });

    it('emit methods are no-ops after disconnect', () => {
      socketService.connect(token, mockDispatch);
      socketService.disconnect();

      socketService.sendMessage('conv-1', 'Hello');
      socketService.markAsRead('msg-1', 'conv-1');
      socketService.startTyping('conv-1');
      socketService.joinGymOccupancy('gym-1');
      socketService.leaveGymOccupancy('gym-1');

      // mockEmit should not have been called after disconnect
      // (it was called 0 times since we cleared mocks in beforeEach
      // and the socket is null after disconnect)
      expect(mockEmit).not.toHaveBeenCalled();
    });
  });
});
