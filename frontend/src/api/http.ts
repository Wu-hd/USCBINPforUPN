import axios, { AxiosError } from 'axios';
import { ElMessage } from 'element-plus';
import type { ApiResponse } from '@/types/api';
import { useAuthStore } from '@/stores/auth';
import { router } from '@/router';

const http = axios.create({
  baseURL: '/',
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>;
    if (!body || typeof body !== 'object' || !('code' in body)) {
      return response.data;
    }

    if (body.code === '00000') {
      return body.data;
    }

    if (body.code === 'AUTH_4010') {
      const authStore = useAuthStore();
      authStore.clearAuth();
      router.replace('/login');
      ElMessage.error(body.message || '登录已失效，请重新登录');
      return Promise.reject(new Error(body.message));
    }

    if (body.code === 'AUTH_4030') {
      router.replace('/403');
      ElMessage.error(body.message || '无权限访问');
      return Promise.reject(new Error(body.message));
    }

    ElMessage.error(body.message || '请求失败');
    return Promise.reject(new Error(body.message || '请求失败'));
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      authStore.clearAuth();
      router.replace('/login');
      ElMessage.error('未认证或会话过期，请重新登录');
    } else if (error.response?.status === 403) {
      router.replace('/403');
      ElMessage.error('无权限访问');
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请重试');
    } else {
      ElMessage.error('网络异常，请稍后重试');
    }
    return Promise.reject(error);
  },
);

export { http };
