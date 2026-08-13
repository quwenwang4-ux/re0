<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'; import { Search } from '@element-plus/icons-vue'; import { listFishesApi } from '../api/fish'; import type { FishInfo } from '../types/api'
const loading = ref(false); const fishes = ref<FishInfo[]>([]); const total = ref(0); const pages = ref(0); const query = reactive({ page: 1, size: 9, keyword: '', category: '' })
async function loadFishes() { loading.value = true; try { const { data } = await listFishesApi(query); fishes.value = data.data.records; total.value = data.data.total; pages.value = data.data.pages } finally { loading.value = false } }
function search() { query.page = 1; loadFishes() } function changePage(page: number) { query.page = page; loadFishes() } onMounted(loadFishes)
</script>
<template>
  <section class="page-hero"><div class="page-container"><p class="eyebrow">MARINE SPECIES ARCHIVE</p><h1>鱼类知识图鉴</h1><p>从名字开始，认识它生活的水域、习性和保护现状。</p></div></section>
  <section class="catalog-section page-container"><form class="search-panel" @submit.prevent="search"><el-input v-model="query.keyword" size="large" clearable placeholder="搜索中文名、学名或关键词"><template #prefix><el-icon><Search /></el-icon></template></el-input><el-input v-model="query.category" size="large" clearable placeholder="分类，例如：鲈形目" /><el-button size="large" type="primary" native-type="submit">查询图鉴</el-button></form>
    <div class="result-meta"><span>共找到 <strong>{{ total }}</strong> 条鱼类资料</span><span>第 {{ query.page }} / {{ Math.max(pages, 1) }} 页</span></div>
    <div v-loading="loading" class="fish-grid"><RouterLink v-for="fish in fishes" :key="fish.id" class="fish-card" :to="`/fishes/${fish.id}`"><div class="fish-image" :style="fish.coverImageUrl ? { backgroundImage: `url(${fish.coverImageUrl})` } : {}"><span v-if="!fish.coverImageUrl">{{ fish.chineseName.slice(0, 1) }}</span><small>{{ fish.protectionLevel || '普通物种' }}</small></div><div class="fish-card-body"><p>{{ fish.category || '分类待完善' }}</p><h2>{{ fish.chineseName }}</h2><em>{{ fish.scientificName || 'Scientific name pending' }}</em><span>{{ fish.habitat || '栖息地信息待完善' }}</span></div></RouterLink><el-empty v-if="!loading && fishes.length === 0" description="暂时没有符合条件的鱼类资料" /></div>
    <el-pagination v-if="total > query.size" background layout="prev, pager, next" :current-page="query.page" :page-size="query.size" :total="total" @current-change="changePage" />
  </section>
</template>
