<script setup lang="ts">
import { h, ref } from 'vue'
import EditUser from './components/EditUser.vue'
import Password from './components/Password.vue'
import Email from './components/Email.vue'

const tabs = [
    { name: '编辑资料', component: EditUser, sep: true },
    { name: '更改密码', component: Password, sep: false },
    { name: '更改邮箱', component: Email, sep: true },
    { name: '注 销', component: h('div', '注销'), sep: false }
]

const selectedMenu = ref(tabs[0].name)
</script>

<template>
  <div class="container">
    <aside class="menu">
      <nav class="menu-nav"
           v-for="tab in tabs"
           :class="[tab.sep ? 'sep' : '', selectedMenu === tab.name ? 'active' : '']"
           @click="() => selectedMenu = tab.name"
      >
        {{ tab.name }}
      </nav>
    </aside>
    <div class="content">
      <component :is="tabs.find(tab => tab.name === selectedMenu).component"/>
    </div>
  </div>
</template>

<style scoped lang="less">
@import "index.less";
</style>
