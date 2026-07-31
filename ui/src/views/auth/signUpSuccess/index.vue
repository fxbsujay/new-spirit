<template>
  <div class="content-box">
    <div class="card">
      <div class="title">
        <v-icon class="mr-2" icon="check-bold" size="3rem"></v-icon>
        <h1>欢迎加入 <strong>Spirit Go</strong></h1>
      </div>
      <p>您好啊 {{ username }}，恭喜您成为我们的一员，赶快前往登录吧</p>
      <RouterLink to="/sign-in">
        <button class="black button">立即登录</button>
      </RouterLink>
    </div>
  </div>
</template>
<script setup>

import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const username = ref('')
if (route.query.username) {
    username.value = route.query.username
}
</script>

<style scoped lang="less">
@import "./index.less";
</style>
