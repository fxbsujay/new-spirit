<template>
  <header class="layout-header">
    <div class="header-content">
      <div class="header-row d-flex align-center">
        <div class="site-title-nav flex-1-1-100">
          <RouterLink to="/" class="site-title">
            <img src="/logo.png" class="logo" alt="logo">
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
        </div>
        <PlayDrawer v-if="!user.visitor"/>
        <div class="tools flex-1-1-100">
          <RouterLink to="/sign-in" v-if="user.visitor">
            登录
          </RouterLink>
          <div class="user flex" v-else>
            <img :src="user.avatar ? '/api/static/avatar/' + user.avatar : '/avatar-error.jpg'" :alt="user.nickname" class="avatar">
            <div class="dropdown">
              <div class="dropdown-content">
                <div class="links">
                  <RouterLink :to="`/@${user.username}`">
                    <Icon name="person" size="14px"/>
                    <span>个人中心</span></RouterLink>
                  <RouterLink @click="logout" class="logout" to="/">
                    <Icon name="logout" size="14px"/>
                    <span>退出登录</span></RouterLink>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <WaitPlayPanels/>

  </header>

  <main class="layout-main">
    <RouterView/>
  </main>
</template>
<script setup>
import { useUserStore } from '@/stores/user.js'
import { ref } from 'vue'
import PlayDrawer from './PlayDrawer.vue'
import WaitPlayPanels from './WaitPlayPanels.vue'

const { user, logout, refreshInfo } = useUserStore()

refreshInfo()
</script>
<style lang="scss" scoped>
@use "./index" as *;
</style>
