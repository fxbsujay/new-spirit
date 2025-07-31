<template>
  <RouterView />
</template>
<script setup>
import { useSocketStore } from '@/stores/socket.js'

const useSocket = useSocketStore()

</script>
<style scoped>
</style>
