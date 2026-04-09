<template>
  <section class="basic-table panel-card">
    <header v-if="$slots.toolbar" class="table-toolbar">
      <slot name="toolbar" :loading="state.loading" :reload="reload" :total="state.total" />
    </header>

    <el-table
      v-loading="state.loading"
      :data="state.list"
      border
      stripe
      :row-key="rowKey"
      class="table-core"
    >
      <slot />

      <template #empty>
        <el-empty description="暂无数据" />
      </template>
    </el-table>

    <footer class="table-pagination">
      <el-pagination
        v-model:current-page="state.pageNum"
        v-model:page-size="state.pageSize"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="state.total"
        :page-sizes="pageSizes"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </footer>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';

type QueryRecord = Record<string, unknown>;

interface RequestParams extends QueryRecord {
  pageNum: number;
  pageSize: number;
}

interface PagedPayload {
  page: {
    pageNum: number;
    pageSize: number;
    total: number;
  };
  list: unknown[];
}

const props = withDefaults(defineProps<{
  requestApi: (params: RequestParams) => Promise<PagedPayload>;
  query?: QueryRecord;
  rowKey?: string;
  immediate?: boolean;
  initialPageSize?: number;
  pageSizes?: number[];
}>(), {
  query: () => ({}),
  rowKey: 'id',
  immediate: true,
  initialPageSize: 10,
  pageSizes: () => [10, 20, 50, 100],
});

const state = reactive({
  loading: false,
  list: [] as unknown[],
  pageNum: 1,
  pageSize: props.initialPageSize,
  total: 0,
});

const loadedOnce = ref(false);

const normalizeQuery = () => {
  const result: QueryRecord = {};
  for (const [key, value] of Object.entries(props.query)) {
    if (value === undefined || value === null || value === '') {
      continue;
    }
    result[key] = value;
  }
  return result;
};

const reload = async () => {
  state.loading = true;
  try {
    const payload = await props.requestApi({
      pageNum: state.pageNum,
      pageSize: state.pageSize,
      ...normalizeQuery(),
    });
    state.list = Array.isArray(payload.list) ? payload.list : [];
    state.pageNum = payload.page?.pageNum || state.pageNum;
    state.pageSize = payload.page?.pageSize || state.pageSize;
    state.total = payload.page?.total || 0;
    loadedOnce.value = true;
  } finally {
    state.loading = false;
  }
};

const resetToFirstPage = async () => {
  state.pageNum = 1;
  await reload();
};

const handleSizeChange = async (size: number) => {
  state.pageSize = size;
  state.pageNum = 1;
  await reload();
};

const handleCurrentChange = async (pageNum: number) => {
  state.pageNum = pageNum;
  await reload();
};

watch(
  () => props.query,
  () => {
    if (!props.immediate && !loadedOnce.value) {
      return;
    }
    void resetToFirstPage();
  },
  { deep: true },
);

onMounted(() => {
  if (props.immediate) {
    void reload();
  }
});

defineExpose({
  reload,
  resetToFirstPage,
});
</script>

<style scoped lang="scss">
.basic-table {
  padding: var(--space-3);
  display: grid;
  gap: var(--space-3);
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}

.table-core {
  width: 100%;
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .table-pagination {
    justify-content: center;
  }
}
</style>
