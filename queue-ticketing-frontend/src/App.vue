<template>
  <div class="app">
    <el-menu
      v-if="showNav"
      mode="horizontal"
      :default-active="route.path"
      router
      class="nav"
    >
      <el-menu-item index="/console">叫号控制台</el-menu-item>
      <el-menu-item index="/config">门店与队列</el-menu-item>
      <el-menu-item index="/simulator">取号模拟</el-menu-item>
      <el-menu-item index="/h5">C 端取号页</el-menu-item>
      <el-menu-item v-if="loggedIn" index="logout" @click="logout">退出登录</el-menu-item>
    </el-menu>

    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { isLoggedIn, removeToken } from './utils/auth';

const route = useRoute();
const router = useRouter();

const showNav = computed(() => route.path !== '/h5' && route.path !== '/login');
const loggedIn = computed(() => isLoggedIn());

const logout = () => {
  removeToken();
  router.push('/login');
};
</script>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background: #f5f7fa;
}

.app {
  min-height: 100vh;
}

.nav {
  margin-bottom: 20px;
}

.page {
  padding: 20px;
}
</style>
