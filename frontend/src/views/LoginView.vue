<template>
  <div class="login-wrap page-shell">
    <section class="brand-column">
      <p class="eyebrow">USCBINP / Task 1</p>
      <h1>城市管网一体化运营平台</h1>
      <p class="description">
        这是一期工程脚手架与基础 UI，已接通统一响应协议、JWT 鉴权链路与菜单接口。
      </p>
      <ul class="pill-list">
        <li>统一响应：code / message / data / timestamp</li>
        <li>鉴权联调：/api/auth/login + /api/auth/me + /api/auth/menus</li>
        <li>主题变量：主色 + 告警红橙黄蓝</li>
      </ul>
    </section>

    <section class="login-card panel-card">
      <h2>登录门户</h2>
      <p>建议账号：demo / demo123</p>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            size="large"
          />
        </el-form-item>

        <el-button type="primary" size="large" class="submit-btn" :loading="authStore.loading" @click="submit">
          进入系统
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus';
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref<FormInstance>();

const form = reactive({
  username: 'demo',
  password: 'demo123',
});

const rules: FormRules<typeof form> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const submit = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  try {
    await authStore.login(form.username, form.password);
    ElMessage.success('登录成功');
    await router.replace('/');
  } catch {
    // Message displayed by interceptor.
  }
};
</script>

<style scoped lang="scss">
.login-wrap {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  align-items: center;
  gap: var(--space-5);
}

.brand-column h1 {
  margin: 0;
  font-size: clamp(34px, 4vw, 52px);
  line-height: 1.1;
  max-width: 10em;
}

.eyebrow {
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-brand-strong);
  font-weight: 700;
}

.description {
  margin-top: var(--space-3);
  color: var(--color-ink-700);
  max-width: 42ch;
}

.pill-list {
  margin-top: var(--space-4);
  padding-left: 18px;
  color: var(--color-ink-700);
}

.pill-list li {
  margin-top: 8px;
}

.login-card {
  padding: var(--space-5);
}

.login-card h2 {
  margin: 0;
  font-size: 28px;
}

.login-card p {
  margin: 8px 0 var(--space-4);
  color: var(--color-ink-700);
}

.submit-btn {
  width: 100%;
  margin-top: var(--space-2);
}

@media (max-width: 960px) {
  .login-wrap {
    grid-template-columns: 1fr;
    padding: var(--space-4) 0;
    gap: var(--space-3);
  }

  .brand-column {
    order: 2;
  }

  .login-card {
    order: 1;
  }
}
</style>
