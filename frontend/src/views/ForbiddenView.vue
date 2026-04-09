<template>
  <div class="state-page page-shell">
    <section class="state-card panel-card">
      <p class="code">403</p>
      <h1>访问受限</h1>
      <p>当前账号没有权限访问该页面，请联系管理员。</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
const goHome = () => router.replace('/');
</script>

<style scoped lang="scss">
.state-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
}

.state-card {
  width: min(560px, 100%);
  padding: var(--space-5);
  text-align: center;
}

.code {
  margin: 0;
  color: var(--alert-orange);
  font-size: 64px;
  font-weight: 700;
}

h1 {
  margin: 0;
}

p {
  margin: var(--space-3) 0 var(--space-4);
  color: var(--color-ink-700);
}
</style>
