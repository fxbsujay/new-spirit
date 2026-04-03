<template>
  <header class="layout-header">
    <div class="header-row flex">
      <div class="site-title-nav flex-1">
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
      <button type="button" class="button playing-btn" :class="drawerVisible ? 'cancel' : ''" @click="drawerVisible = !drawerVisible">
        {{ drawerVisible ? '取消' : '开始游戏' }}
      </button>
      <div class="tools flex-1">
        <RouterLink to="/sign-in" v-if="user.visitor">
          登录
        </RouterLink>
        <div class="user flex" v-else>
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
    <PlayDrawer :visible="drawerVisible" />
    <WaitPlayPanels />
  </header>
  <main class="layout-main">
    <RouterView/>
  </main>
</template>
<script setup>
import { ref } from 'vue'
import PlayDrawer from './PlayDrawer.vue'
import WaitPlayPanels from './WaitPlayPanels.vue'
import { useUserStore } from '@/stores/user.js'
const { user, logout, refreshInfo } = useUserStore()

const drawerVisible = ref(false)
refreshInfo()
</script>
<style lang="less" scoped>
@import "./index.less";
</style>
