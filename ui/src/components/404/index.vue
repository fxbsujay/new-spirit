<template>
  <div class="not-found">
    <div class="card">
      <div class="code">404</div>
      <h1 class="title">{{ title }}</h1>
      <p class="desc">{{ desc }}</p>
      <p v-if="code" class="game-code">
        <span class="label">对局编号</span>
        <span class="value">{{ code }}</span>
      </p>
      <p v-else class="path">{{ route.fullPath }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

/**
 * 对局编号：由 /404?code=xxxx 传入，用于展示「未找到对局」
 */
const code = computed(() => route.query.code || '')

const title = computed(() => code.value ? '未找到对局' : '页面不存在')

const desc = computed(() => code.value ? '该对局不存在或已被删除，请检查对局编号是否正确' : '没有找到与你访问的地址对应的页面，它可能已经被删除或移动了')
</script>

<style scoped lang="scss">
@use "./index" as *;
</style>
