<script setup lang="ts">
import { h, ref } from 'vue'
import EditUser from './components/EditUser.vue'
import Password from './components/Password.vue'
import Email from './components/Email.vue'

const tabs = [
    { name: '编辑资料', component: EditUser },
    { name: '更改密码', component: Password },
    { name: '更改邮箱', component: Email },
    { name: '注 销', component: h('div', '注销') },
]

const selectedMenu = ref(tabs[0].name)
</script>

<template>

  <div class="account-box">
    <div class="d-flex flex-row account-box">
      <aside class="menu">
        <nav class="menu-nav"
             v-for="tab in tabs"
             :class="selectedMenu === tab.name ? 'active' : ''"
             @click="() => selectedMenu = tab.name"
        >
          {{ tab.name }}
        </nav>
      </aside>
      <div class="content">
        <component :is="tabs.find(tab => tab.name === selectedMenu).component"/>
      </div>
    </div>
  </div>

</template>

<style scoped lang="scss">
@use "./index" as *;
</style>
