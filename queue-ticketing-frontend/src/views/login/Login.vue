<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <span>商户后台登录</span>
      </template>
      <el-form :model="form" label-width="80px" @keyup.enter="login">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="默认 admin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="默认 123456" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="login">登录</el-button>
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" title="演示账号：admin / 123456" />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { staffApi } from '../../api';
import { setToken } from '../../utils/auth';

const router = useRouter();
const loading = ref(false);
const form = reactive({
  username: 'admin',
  password: '123456'
});

const login = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码');
    return;
  }
  loading.value = true;
  try {
    const token = await staffApi.login(form);
    setToken(token);
    ElMessage.success('登录成功');
    router.push('/console');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.login-card {
  width: 400px;
}
</style>
