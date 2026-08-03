<template>
  <div class="content-box">
    <div class="card">
      <div class="title">
        <v-icon class="mr-2" icon="check-bold" size="4rem" color="green"></v-icon>
        <h1>欢迎加入 <span class="font-weight-black">Spirit Go</span></h1>
      </div>
      <p>您好啊 <span class="font-weight-bold text-primary">{{ username }}</span>，恭喜您成为我们的一员，赶快前往登录吧</p>
      <RouterLink to="/sign-in">
        <v-btn block class="mt-8" color="black" size="large">立即登录</v-btn>
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

<style scoped lang="sass">
@use 'index.sass'
</style>
