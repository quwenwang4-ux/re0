<script setup lang="ts">
import { onMounted, ref } from 'vue'; import { useRoute } from 'vue-router'; import { ArrowLeft } from '@element-plus/icons-vue'; import { getFishApi } from '../api/fish'; import type { FishInfo } from '../types/api'
const route = useRoute(); const loading = ref(true); const fish = ref<FishInfo | null>(null)
onMounted(async () => { try { const { data } = await getFishApi(Number(route.params.id)); fish.value = data.data } finally { loading.value = false } })
</script>
<template>
  <section v-loading="loading" class="detail-page page-container"><RouterLink class="back-link" to="/fishes"><el-icon><ArrowLeft /></el-icon>返回鱼类图鉴</RouterLink>
    <template v-if="fish"><div class="detail-heading"><div><p class="eyebrow dark">{{ fish.category || 'MARINE SPECIES' }}</p><h1>{{ fish.chineseName }}</h1><em>{{ fish.scientificName || '学名待补充' }}</em></div><el-tag size="large" effect="dark">{{ fish.protectionLevel || '普通物种' }}</el-tag></div>
      <div class="detail-grid"><div class="detail-image" :style="fish.coverImageUrl ? { backgroundImage: `url(${fish.coverImageUrl})` } : {}"><span v-if="!fish.coverImageUrl">{{ fish.chineseName.slice(0, 1) }}</span></div><div class="detail-facts"><article><small>外形特征</small><p>{{ fish.appearance || '暂无资料' }}</p></article><article><small>生活习性</small><p>{{ fish.habits || '暂无资料' }}</p></article><article><small>栖息环境</small><p>{{ fish.habitat || '暂无资料' }}</p></article><article><small>地理分布</small><p>{{ fish.distribution || '暂无资料' }}</p></article></div></div>
      <div class="source-note"><span>资料来源</span><strong>{{ fish.sourceType }}</strong><p>{{ fish.sourceDescription || '系统鱼类资料库' }}</p></div>
    </template><el-empty v-else-if="!loading" description="没有找到这条鱼类资料" />
  </section>
</template>
