export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ErrorResponse {
  error: string;
  message: string;
  timestamp: string;
  traceId: string;
}
