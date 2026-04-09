<template>
  <div class="layout page-shell">
    <aside class="layout-aside panel-card">
      <div class="logo-block">
        <p class="logo-main">USCBINP</p>
        <p class="logo-sub">一期演示门户</p>
      </div>

      <nav class="menu-list">
        <button
          v-for="item in menuItems"
          :key="item.code"
          class="menu-item"
          :class="{ active: isActive(item) }"
          type="button"
          @click="jump(item)"
        >
          {{ item.label }}
        </button>
      </nav>
    </aside>

    <section class="layout-content">
      <header class="layout-header panel-card">
        <div>
          <h1>运行中控台</h1>
          <p>基础 UI、通用组件与后端联调均已接通</p>
        </div>

        <div class="header-actions">
          <span class="user-chip">{{ authStore.username || '访客' }}</span>
          <el-button type="danger" plain @click="logout">退出登录</el-button>
        </div>
      </header>

      <main class="layout-main panel-card">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

interface MenuItem {
  code: string;
  label: string;
  path: string;
}

const MENU_ROUTE_MAP: Record<string, MenuItem> = {
  'system:dashboard': {
    code: 'system:dashboard',
    label: '运行中控台',
    path: '/',
  },
  'asset:network:list': {
    code: 'asset:network:list',
    label: '管网档案',
    path: '/asset/networks',
  },
};

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const menuItems = computed(() => {
  const sourceCodes = authStore.menus.length
    ? Array.from(new Set([...authStore.menus, 'asset:network:list']))
    : ['system:dashboard', 'asset:network:list'];

  return sourceCodes.map((code) => {
    if (MENU_ROUTE_MAP[code]) {
      return MENU_ROUTE_MAP[code];
    }
    return {
      code,
      label: code,
      path: '',
    };
  });
});

const isActive = (item: MenuItem) => {
  if (!item.path) {
    return false;
  }
  return route.path === item.path;
};

const jump = (item: MenuItem) => {
  if (!item.path || route.path === item.path) {
    return;
  }
  router.push(item.path);
};

const logout = () => {
  authStore.clearAuth();
  router.replace('/login');
};
</script>

<style scoped lang="scss">
.layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: var(--space-4);
  padding: var(--space-4) 0;
  min-height: 100vh;
}

.layout-aside {
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.logo-main {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: var(--color-brand-strong);
}

.logo-sub {
  margin: var(--space-1) 0 0;
  color: var(--color-ink-700);
  font-size: 14px;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.menu-item {
  text-align: left;
  border: 0;
  padding: 11px 14px;
  border-radius: var(--radius-sm);
  font: inherit;
  background: rgba(14, 143, 127, 0.08);
  color: var(--color-brand-strong);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.menu-item:hover {
  transform: translateX(3px);
  background: rgba(14, 143, 127, 0.14);
}

.menu-item.active {
  background: linear-gradient(90deg, rgba(14, 143, 127, 0.2), rgba(215, 154, 51, 0.18));
  box-shadow: inset 0 0 0 1px rgba(14, 143, 127, 0.38);
}

.layout-content {
  display: grid;
  grid-template-rows: auto 1fr;
  gap: var(--space-3);
  min-width: 0;
}

.layout-header {
  padding: var(--space-3) var(--space-4);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.layout-header h1 {
  margin: 0;
  font-size: 24px;
}

.layout-header p {
  margin: 6px 0 0;
  color: var(--color-ink-700);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.user-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(19, 33, 55, 0.08);
  color: var(--color-ink-900);
}

.layout-main {
  min-height: 0;
  padding: var(--space-4);
}

@media (max-width: 960px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .layout-aside {
    order: 2;
  }

  .layout-content {
    order: 1;
  }

  .layout-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
