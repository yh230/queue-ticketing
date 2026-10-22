<template>
  <div class="app">
    <div v-if="showNav" class="nav-bar">
      <el-menu
        mode="horizontal"
        :default-active="route.path"
        router
        class="nav"
      >
        <el-menu-item index="/console">叫号控制台</el-menu-item>
        <el-menu-item index="/config">门店与队列</el-menu-item>
        <el-menu-item index="/simulator">取号模拟</el-menu-item>
        <el-menu-item index="/h5">C 端取号页</el-menu-item>
      </el-menu>
      <el-button v-if="loggedIn" link type="danger" class="logout" @click="logout">
        退出登录
      </el-button>
    </div>

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

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 20px;
  background: #fff;
  margin-bottom: 20px;
}

.nav {
  flex: 1;
  border-bottom: none;
}

.logout {
  margin-left: 12px;
}

.page {
  padding: 20px;
}
</style>
