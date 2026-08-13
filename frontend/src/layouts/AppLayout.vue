<script setup lang="ts">
import { useRouter } from 'vue-router'
import { User, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
const router = useRouter(); const auth = useAuthStore()
function logout() { auth.logout(); router.push('/') }
</script>
<template>
  <div class="app-shell">
    <header class="site-header">
      <RouterLink class="brand" to="/"><span class="brand-mark">蓝</span><span><strong>蓝境</strong><small>SeaFish Intelligence</small></span></RouterLink>
      <nav class="main-nav" aria-label="主导航"><RouterLink to="/">首页</RouterLink><RouterLink to="/fishes">鱼类图鉴</RouterLink><RouterLink to="/detect">智能识别</RouterLink><RouterLink to="/rescue">公益救助</RouterLink><RouterLink v-if="auth.isLoggedIn" to="/fish-applications">资料反馈</RouterLink><RouterLink v-if="auth.isLoggedIn" to="/volunteer">志愿者</RouterLink><RouterLink v-if="auth.isAdmin" to="/admin">管理后台</RouterLink><RouterLink v-if="auth.isAdmin" to="/admin/fishes">资料管理</RouterLink></nav>
      <div class="header-actions">
        <template v-if="auth.isLoggedIn"><RouterLink class="user-link" to="/profile"><el-icon><User /></el-icon>{{ auth.displayName }}</RouterLink><button class="text-button" type="button" @click="logout"><el-icon><SwitchButton /></el-icon>退出</button></template>
        <template v-else><RouterLink class="text-link" to="/login">登录</RouterLink><RouterLink class="primary-link" to="/register">加入蓝境</RouterLink></template>
      </div>
    </header>
    <main><RouterView /></main>
    <footer class="site-footer"><div><strong>蓝境</strong><span>让每一次认识，都成为保护的开始。</span></div><small>海洋鱼类智能化信息管理系统 · Java + Vue 学习项目</small></footer>
  </div>
</template>
