<script setup lang="ts">import { ArrowRight, Camera, FirstAidKit, Collection } from '@element-plus/icons-vue'</script>
<template>
  <section class="hero-section"><div class="hero-orbit orbit-one"></div><div class="hero-orbit orbit-two"></div><div class="hero-content page-container">
    <p class="eyebrow">OCEAN KNOWLEDGE · AI RECOGNITION · PUBLIC WELFARE</p><h1>读懂海洋生命，<br><em>连接每一次守护。</em></h1>
    <p class="hero-copy">浏览可信的海洋鱼类资料，用智能识别发现物种，也可以上报环境问题与受伤动物，让知识真正走向保护行动。</p>
    <div class="hero-buttons"><RouterLink class="hero-primary" to="/fishes">探索鱼类图鉴 <el-icon><ArrowRight /></el-icon></RouterLink><RouterLink class="hero-secondary" to="/login">登录后体验识别</RouterLink></div>
    <div class="hero-stats"><div><strong>知识</strong><span>鱼类资料持续收录</span></div><div><strong>识别</strong><span>图片智能识别记录</span></div><div><strong>行动</strong><span>公益救助工单闭环</span></div></div>
  </div></section>
  <section class="feature-section page-container"><div class="section-heading"><p class="eyebrow dark">FROM KNOWING TO PROTECTING</p><h2>不只是一个鱼类资料库</h2><p>系统把认识、发现、反馈和救助连接成一条完整路径。</p></div>
    <div class="feature-grid">
      <article><span class="feature-number">01</span><el-icon><Collection /></el-icon><h3>鱼类知识图鉴</h3><p>按名称、分类和关键词查询物种，查看学名、习性、栖息地、分布和保护等级。</p><RouterLink to="/fishes">开始浏览 <el-icon><ArrowRight /></el-icon></RouterLink></article>
      <article><span class="feature-number">02</span><el-icon><Camera /></el-icon><h3>AI 智能识别</h3><p>上传图片生成识别记录，展示候选结果；未收录物种自动进入管理员待办。</p><RouterLink to="/login">登录体验 <el-icon><ArrowRight /></el-icon></RouterLink></article>
      <article><span class="feature-number">03</span><el-icon><FirstAidKit /></el-icon><h3>海洋公益救助</h3><p>上报环境或动物救助问题，经管理员审批后由志愿者接单并反馈处理结果。</p><RouterLink to="/login">参与守护 <el-icon><ArrowRight /></el-icon></RouterLink></article>
    </div>
  </section>
  <section class="mission-band"><div class="page-container"><p>OUR MISSION</p><blockquote>“技术让我们看见，行动让海洋改变。”</blockquote><span>每一条资料、每一次识别、每一张工单，都是这个系统可追踪的数据。</span></div></section>
</template>
