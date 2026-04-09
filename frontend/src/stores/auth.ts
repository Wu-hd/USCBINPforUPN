import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { loginApi, meApi, menusApi } from '@/api/auth';

const TOKEN_KEY = 'uscbinp_token';

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '');
  const username = ref('');
  const menus = ref<string[]>([]);
  const loading = ref(false);

  const isLoggedIn = computed(() => token.value.length > 0);

  const setToken = (value: string) => {
    token.value = value;
    localStorage.setItem(TOKEN_KEY, value);
  };

  const clearAuth = () => {
    token.value = '';
    username.value = '';
    menus.value = [];
    localStorage.removeItem(TOKEN_KEY);
  };

  const login = async (account: string, password: string) => {
    loading.value = true;
    try {
      const result = await loginApi({ username: account, password });
      setToken(result.token);
      await bootstrap();
    } finally {
      loading.value = false;
    }
  };

  const bootstrap = async () => {
    if (!token.value) {
      return;
    }
    const [me, userMenus] = await Promise.all([meApi(), menusApi()]);
    username.value = me;
    menus.value = userMenus;
  };

  return {
    token,
    username,
    menus,
    loading,
    isLoggedIn,
    login,
    bootstrap,
    clearAuth,
  };
});
