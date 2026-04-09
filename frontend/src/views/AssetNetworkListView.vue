<template>
  <section class="asset-page">
    <header class="asset-header">
      <div>
        <p class="asset-eyebrow">Task 2 / 通用组件沉淀</p>
        <h2>管网档案列表</h2>
        <p>基于 SearchPanel + BasicTable + StatusTag，已联调后端分页接口。</p>
      </div>
      <el-button plain @click="refresh">刷新</el-button>
    </header>

    <SearchPanel :model="queryForm" @search="search" @reset="reset">
      <el-form-item label="区域编码">
        <el-input v-model="queryForm.regionCode" clearable placeholder="例如 3302（必填）" />
      </el-form-item>
    </SearchPanel>

    <BasicTable ref="tableRef" :request-api="requestApi" :query="queryParams" row-key="id">
      <template #toolbar="{ total }">
        <div class="table-summary">
          <span>当前检索命中</span>
          <strong>{{ total }}</strong>
          <span>条记录</span>
        </div>
      </template>

      <el-table-column type="index" label="#" width="70" />
      <el-table-column prop="networkCode" label="管网编码" min-width="180" />
      <el-table-column prop="networkName" label="管网名称" min-width="200" />

      <el-table-column label="类型" min-width="130">
        <template #default="scope">
          <StatusTag :value="scope.row.networkType" :dict="networkTypeDict" unknown-label="未定义" />
        </template>
      </el-table-column>

      <el-table-column prop="regionCode" label="区域编码" min-width="120" />

      <el-table-column label="服务状态" min-width="120">
        <template #default="scope">
          <StatusTag :value="scope.row.serviceStatus" :dict="serviceStatusDict" />
        </template>
      </el-table-column>
    </BasicTable>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import BasicTable from '@/components/BasicTable.vue';
import SearchPanel from '@/components/SearchPanel.vue';
import StatusTag from '@/components/StatusTag.vue';
import { listAssetNetworksApi } from '@/api/asset';
import type { PagedResult } from '@/types/api';
import type { AssetNetworkItem, AssetNetworkListParams } from '@/types/asset';

interface BasicTableExpose {
  reload: () => Promise<void>;
  resetToFirstPage: () => Promise<void>;
}

const tableRef = ref<BasicTableExpose | null>(null);

const queryForm = reactive({
  regionCode: '3302',
});

const queryParams = computed(() => ({
  regionCode: queryForm.regionCode.trim() || undefined,
}));

const serviceStatusDict = {
  '0': { label: '停运', type: 'danger' as const },
  '1': { label: '在服', type: 'success' as const },
  '2': { label: '检修', type: 'warning' as const },
};

const networkTypeDict = {
  MAIN: { label: '主干网', type: 'primary' as const },
  BRANCH: { label: '支线网', type: 'info' as const },
};

const requestApi = (params: AssetNetworkListParams): Promise<PagedResult<AssetNetworkItem>> => {
  return listAssetNetworksApi(params);
};

const refresh = async () => {
  await tableRef.value?.reload();
};

const search = async () => {
  await tableRef.value?.resetToFirstPage();
};

const reset = async () => {
  queryForm.regionCode = '3302';
  await tableRef.value?.resetToFirstPage();
};
</script>

<style scoped lang="scss">
.asset-page {
  display: grid;
  gap: var(--space-3);
}

.asset-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
}

.asset-eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-brand-strong);
  font-weight: 700;
  font-size: 12px;
}

.asset-header h2 {
  margin: 6px 0 0;
  font-size: clamp(24px, 3vw, 34px);
}

.asset-header p {
  margin: 8px 0 0;
  color: var(--color-ink-700);
}

.table-summary {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
  color: var(--color-ink-700);
}

.table-summary strong {
  color: var(--color-brand-strong);
  font-size: 24px;
  line-height: 1;
}

@media (max-width: 900px) {
  .asset-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
