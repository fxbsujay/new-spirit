<script setup>
import slider from '@/components/slider/index.vue'
import {ref} from 'vue'

const value = ref(20)
const value2 = ref(20)
</script>

<template>
  <div style="padding: 1rem">
    <h1>{{ value }}</h1>
    <slider v-model="value"/>
  </div>
</template>

<style scoped lang="less">

</style>