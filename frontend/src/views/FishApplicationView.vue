<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createFishApplicationApi, listMyFishApplicationsApi } from '../api/fishApplication'
import type { FishInfoApplication } from '../types/business'

const records = ref<FishInfoApplication[]>([])
const submitting = ref(false)
const form = reactive({ applicationType: 'ADD' as 'ADD'|'CORRECTION', targetFishId: null as number|null, chineseName: '', scientificName: '', category: '', appearance: '', habits: '', habitat: '', distribution: '', protectionLevel: '', coverImageUrl: '', sourceDescription: '', reason: '' })

async function load() { records.value = (await listMyFishApplicationsApi()).data.data.records }
async function submit() {
  submitting.value = true
  try {
    await createFishApplicationApi(form)
    ElMessage.success('鱼类资料申请已提交')
    Object.assign(form, { applicationType:'ADD', targetFishId:null, chineseName:'', scientificName:'', category:'', appearance:'', habits:'', habitat:'', distribution:'', protectionLevel:'', coverImageUrl:'', sourceDescription:'', reason:'' })
    load()
  } finally { submitting.value = false }
}
onMounted(load)
</script>

<template><section class="page-hero compact"><div class="page-container"><p class="eyebrow">COMMUNITY KNOWLEDGE</p><h1>鱼类资料反馈</h1><p>用户提交新增或纠错申请，由管理员确认后再写入鱼类数据库。</p></div></section><section class="business-page page-container"><div class="application-layout"><section><h2>提交资料申请</h2><el-form :model="form" label-position="top"><div class="form-row"><el-form-item label="申请类型"><el-select v-model="form.applicationType" style="width:100%"><el-option label="新增鱼类" value="ADD"/><el-option label="纠正已有资料" value="CORRECTION"/></el-select></el-form-item><el-form-item v-if="form.applicationType==='CORRECTION'" label="目标鱼类 ID"><el-input-number v-model="form.targetFishId" :min="1"/></el-form-item></div><div class="form-row"><el-form-item label="中文名"><el-input v-model="form.chineseName"/></el-form-item><el-form-item label="学名"><el-input v-model="form.scientificName"/></el-form-item></div><div class="form-row"><el-form-item label="分类"><el-input v-model="form.category"/></el-form-item><el-form-item label="保护等级"><el-input v-model="form.protectionLevel"/></el-form-item></div><el-form-item label="外形特征"><el-input v-model="form.appearance" type="textarea"/></el-form-item><el-form-item label="生活习性"><el-input v-model="form.habits" type="textarea"/></el-form-item><div class="form-row"><el-form-item label="栖息地"><el-input v-model="form.habitat"/></el-form-item><el-form-item label="地理分布"><el-input v-model="form.distribution"/></el-form-item></div><el-form-item label="资料来源说明"><el-input v-model="form.sourceDescription"/></el-form-item><el-form-item label="申请原因"><el-input v-model="form.reason" type="textarea" :rows="3"/></el-form-item><el-button type="primary" size="large" :loading="submitting" @click="submit">提交管理员审核</el-button></el-form></section><aside><h2>我的申请记录</h2><article v-for="item in records" :key="item.id"><div><strong>{{item.chineseName}}</strong><el-tag>{{item.status}}</el-tag></div><p>{{item.applicationType==='ADD'?'新增资料':'纠正资料'}} · {{item.reason}}</p><span>{{item.reviewComment||'等待管理员审核'}}</span></article><el-empty v-if="records.length===0" description="暂无资料申请"/></aside></div></section></template>
