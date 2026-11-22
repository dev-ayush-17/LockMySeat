// API client and configuration for LockMySeat backend

export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api';

export interface User {
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface Event {
  id: number;
  title: string;
  description: string;
  city: string;
  venue: string;
  eventDate: string;
  totalSeats: number;
  availableSeats: number;
}

export interface Seat {
  id: number;
  eventId: number;
  seatNumber: string;
  rowLabel: string;
  colNumber: number;
  seatType: 'VIP' | 'REGULAR';
  status: 'AVAILABLE' | 'BOOKED';
  price: number;
}

export interface BookingItem {
  id: number;
  bookingId: number;
  seatId: number;
  price: number;
  seatNumber: string;
}

export interface Booking {
  id: number;
  userId: number;
  eventId: number;
  totalAmount: number;
  status: 'CONFIRMED' | 'CANCELLED';
  bookingDate: string;
  items: BookingItem[];
  eventTitle?: string;
  eventCity?: string;
  eventDate?: string;
}

export function getStoredToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('lms_token');
}

export function getStoredUser(): User | null {
  if (typeof window === 'undefined') return null;
  const raw = localStorage.getItem('lms_user');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function setSession(token: string, user: User) {
  localStorage.setItem('lms_token', token);
  localStorage.setItem('lms_user', JSON.stringify(user));
}

export function clearSession() {
  localStorage.removeItem('lms_token');
  localStorage.removeItem('lms_user');
}

export async function apiRequest<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getStoredToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.message || `Request failed with status ${response.status}`);
  }

  return data as T;
}
