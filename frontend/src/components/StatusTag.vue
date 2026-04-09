<template>
  <el-tag
    :type="resolvedMeta.type ?? unknownType"
    :effect="resolvedMeta.effect ?? 'light'"
    :color="resolvedMeta.color"
  >
    {{ resolvedMeta.label ?? unknownLabel }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { TagProps } from 'element-plus';

interface StatusMeta {
  label: string;
  type?: TagProps['type'];
  effect?: TagProps['effect'];
  color?: string;
}

type StatusDict = Record<string, StatusMeta>;

const props = withDefaults(defineProps<{
  value: string | number | null | undefined;
  dict: StatusDict;
  unknownLabel?: string;
  unknownType?: TagProps['type'];
}>(), {
  unknownLabel: '未知',
  unknownType: 'info',
});

const statusKey = computed(() => {
  if (props.value === null || props.value === undefined || props.value === '') {
    return '__empty__';
  }
  return String(props.value);
});

const resolvedMeta = computed(() => props.dict[statusKey.value] ?? { label: props.unknownLabel });
</script>
