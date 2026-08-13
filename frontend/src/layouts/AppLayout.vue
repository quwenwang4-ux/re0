<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Search, User, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
const router = useRouter(); const auth = useAuthStore()
function logout() { auth.logout(); router.push('/') }
</script>
<template>
  <div class="app-shell">
    <header class="site-header">
      <RouterLink class="brand" to="/"><span class="brand-mark">蓝</span><span><strong>蓝境</strong><small>SeaFish Intelligence</small></span></RouterLink>
      <nav class="main-nav" aria-label="主导航"><RouterLink to="/">首页</RouterLink><RouterLink to="/fishes">鱼类图鉴</RouterLink><RouterLink to="/fishes"><el-icon><Search /></el-icon>探索</RouterLink></nav>
      <div class="header-actions">
        <template v-if="auth.isLoggedIn"><RouterLink class="user-link" to="/profile"><el-icon><User /></el-icon>{{ auth.displayName }}</RouterLink><button class="text-button" type="button" @click="logout"><el-icon><SwitchButton /></el-icon>退出</button></template>
        <template v-else><RouterLink class="text-link" to="/login">登录</RouterLink><RouterLink class="primary-link" to="/register">加入蓝境</RouterLink></template>
      </div>
    </header>
    <main><RouterView /></main>
    <footer class="site-footer"><div><strong>蓝境</strong><span>让每一次认识，都成为保护的开始。</span></div><small>海洋鱼类智能化信息管理系统 · Java + Vue 学习项目</small></footer>
  </div>
</template>
