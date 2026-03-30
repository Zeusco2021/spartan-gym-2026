export interface ConversationParticipant {
  userId: string;
  name: string;
  profilePhotoUrl?: string;
  online: boolean;
}

export interface Conversation {
  id: string;
  participantIds: string[];
  type: 'direct' | 'group';
  lastMessageAt: string;
  unreadCount: number;
  participants: ConversationParticipant[];
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  contentType: 'text' | 'image' | 'file';
  status: 'sending' | 'sent' | 'delivered' | 'read';
  sentAt: string;
  readAt?: string;
}
