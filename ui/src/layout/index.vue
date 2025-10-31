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
        <RouterLink to="/sign-in" v-if="user.isGuest">
          登录
        </RouterLink>
        <div class="user" v-else>
          <img :src="user.avatar" :alt="user.nickname" class="avatar">
          <div class="dropdown">
            <div class="dropdown-content">
              <div class="links">
                <RouterLink to="/sign-in"><Icon name="person" size="14px" /><span>个人中心</span></RouterLink>
                <RouterLink @click="logout" class="logout" to="/"><Icon name="logout" size="14px" /><span>退出登录</span></RouterLink>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
  <main class="layout-main">
    <RouterView/>
  </main>
</template>
<script setup>
import { useUserStore } from '@/stores/user.js'
const { user, logout, refreshInfo } = useUserStore()

refreshInfo()

</script>
<style lang="less" scoped>
@import "./index.less";
</style>
