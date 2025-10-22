<template>
  <header class="layout-header">
    <div class="site-title-nav">
      <RouterLink to="/">
        <div class="site-title">
          <img src="/logo.png" class="logo" alt="logo">
        </div>
      </RouterLink>
      <nav class="nav-bar">
        <section>
          <RouterLink to="/sign-in">下棋</RouterLink>
          <div class="group">
            <RouterLink to="/sign-in">创建对局</RouterLink>
            <RouterLink to="/sign-in">锦标赛</RouterLink>
            <RouterLink to="/sign-in">锦标赛</RouterLink>
          </div>
        </section>
        <section>
          <RouterLink to="/sign-in">工具</RouterLink>
          <div class="group">
            <RouterLink to="/sign-in">创建对局</RouterLink>
            <RouterLink to="/sign-in">锦标赛</RouterLink>
            <RouterLink to="/sign-in">锦标赛</RouterLink>
          </div>
        </section>
      </nav>
      <div class="tools">
        <RouterLink to="/sign-in">
          登录
        </RouterLink>
      </div>
    </div>
  </header>
  <main class="layout-main">
    <RouterView/>
  </main>
</template>
<script setup>
import Loading from '@/components/loading/index.vue'
</script>
<style lang="less" scoped>
@import "./index.less";
</style>
