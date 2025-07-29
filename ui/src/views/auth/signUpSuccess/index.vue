<template>
  <div class="content-box">
    <div class="card">
      <div class="title">
        <Icon name="check-bold" color="#71A339" size="3rem" />
        <h1>欢迎加入 <strong>Spirit Go</strong></h1>
      </div>
      <p>您好啊 {{username }}，恭喜您成为我们的一员，赶快前往登录吧</p>
      <RouterLink to="/sign-in">
        <button class="button primary">立即登录</button>
      </RouterLink>
    </div>
  </div>
</template>
<script setup>

import { ref } from 'vue'
import { useRoute } from 'vue-router'

const params = useRoute().params
const username = ref('')

if (params.username) {
  username.value = params.username
}
</script>

<style scoped lang="less">
@import "./index.less";
</style>