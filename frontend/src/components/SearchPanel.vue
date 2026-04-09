<template>
  <section class="search-panel panel-card">
    <el-form :model="model" label-position="top" class="search-form" @submit.prevent>
      <div class="search-fields">
        <slot />
      </div>

      <div class="search-actions">
        <el-button type="primary" :loading="loading" @click="emit('search')">查询</el-button>
        <el-button @click="emit('reset')">重置</el-button>
        <slot name="actions" />
      </div>
    </el-form>
  </section>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  model?: Record<string, unknown>;
  loading?: boolean;
}>(), {
  model: () => ({}),
  loading: false,
});

const emit = defineEmits<{
  search: [];
  reset: [];
}>();
</script>

<style scoped lang="scss">
.search-panel {
  padding: var(--space-3) var(--space-4);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-3);
}

.search-fields {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: var(--space-2) var(--space-3);
  flex: 1;
  min-width: min(560px, 100%);
}

.search-fields :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

@media (max-width: 900px) {
  .search-form {
    align-items: stretch;
  }

  .search-fields {
    min-width: 100%;
  }

  .search-actions {
    width: 100%;
  }

  .search-actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
