import { http } from './http';
import type { LoginPayload, LoginResult } from '@/types/auth';

export const loginApi = (payload: LoginPayload) => {
  return http.post<never, LoginResult>('/api/auth/login', payload);
};

export const meApi = () => {
  return http.get<never, string>('/api/auth/me');
};

export const menusApi = () => {
  return http.get<never, string[]>('/api/auth/menus');
};

export const healthApi = () => {
  return http.get<never, string>('/api/health');
};
