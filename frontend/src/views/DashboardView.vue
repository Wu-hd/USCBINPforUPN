<template>
  <section class="dashboard">
    <div class="health-grid">
      <article class="metric-card">
        <p class="label">后端健康</p>
        <h3>{{ healthStatus }}</h3>
      </article>
      <article class="metric-card">
        <p class="label">当前用户</p>
        <h3>{{ authStore.username || '-' }}</h3>
      </article>
      <article class="metric-card">
        <p class="label">菜单数量</p>
        <h3>{{ authStore.menus.length }}</h3>
      </article>
    </div>

    <div class="legend panel-card">
      <h4>告警色标变量</h4>
      <div class="legend-row">
        <span class="dot red"></span>
        <span>红色 High</span>
      </div>
      <div class="legend-row">
        <span class="dot orange"></span>
        <span>橙色 Medium</span>
      </div>
      <div class="legend-row">
        <span class="dot yellow"></span>
        <span>黄色 Low</span>
      </div>
      <div class="legend-row">
        <span class="dot blue"></span>
        <span>蓝色 Info</span>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { healthApi } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const healthStatus = ref('CHECKING...');

onMounted(async () => {
  try {
    const status = await healthApi();
    healthStatus.value = status;
  } catch {
    healthStatus.value = 'UNREACHABLE';
  }
});
</script>

<style scoped lang="scss">
.dashboard {
  display: grid;
  gap: var(--space-4);
}

.health-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.metric-card {
  border-radius: var(--radius-md);
  padding: var(--space-3);
  background: linear-gradient(160deg, rgba(14, 143, 127, 0.15), rgba(14, 143, 127, 0.04));
  border: 1px solid rgba(14, 143, 127, 0.2);
}

.metric-card .label {
  margin: 0;
  color: var(--color-ink-700);
}

.metric-card h3 {
  margin: 10px 0 0;
  font-size: clamp(22px, 2.5vw, 30px);
}

.legend {
  padding: var(--space-4);
}

.legend h4 {
  margin: 0 0 var(--space-2);
}

.legend-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.red { background: var(--alert-red); }
.orange { background: var(--alert-orange); }
.yellow { background: var(--alert-yellow); }
.blue { background: var(--alert-blue); }

@media (max-width: 960px) {
  .health-grid {
    grid-template-columns: 1fr;
  }
}
</style>
