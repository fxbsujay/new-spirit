
<script setup>
import Responsive from '@/components/responsive/index.vue'
import CreateDialog from './CreateDialog.vue'
import { ref, useTemplateRef } from 'vue'
import Icon from '@/components/icon/Icon.vue'
import http from '@/utils/http.js'

const createDialogRef = useTemplateRef('createDialogRef')

const games = ref([])

http.get('/game/search').then(res => {
  games.value = res
}).catch(err => {
  games.value = []
})
</script>

<template>
  <div class="lobby-wrap">
    <div class="lobby-side">
      <div class="">

      </div>
    </div>
    <div class="lobby-table">
      <div class="toggle-filter">
        <div class="search-wrap">
          <div class="btn-icon">
            <Icon name="search" size="18px"/>
          </div>
          <input class="search-input" type="text" placeholder="搜索房间名称或房间号"/>
        </div>
        <div class="btn-group">
          <div class="btn-icon">
            <Icon name="refresh" size="18px"/>
          </div>
          <div class="btn-icon">
            <Icon name="settings" size="18px"/>
          </div>
        </div>
      </div>
      <div class="lists">
        <div class="item" v-for="item in games">
          <div class="col name-col">
            <p class="name">{{ item.name }}</p>
            <p class="user">我是莲花</p>
          </div>
          <div class="col">{{ item.boardSize }}x{{ item.boardSize }}</div>
          <div class="col">10h+6s</div>
          <div class="col">1700</div>
          <div class="col">
            <Icon name="lock" size="1rem"/>
          </div>
        </div>
      </div>
    </div>
    <div class="lobby-play">
      <Responsive :aspect-ratio="0.5">
        <div class="banner">
          <img alt=""/>
        </div>
      </Responsive>
      <div class="play-btn">
        <button class="button border" @click="createDialogRef.open()">创建游戏</button>
        <button class="button border">快速匹配</button>
        <button class="button border">积分赛</button>
      </div>
    </div>
  </div>
  <CreateDialog  ref="createDialogRef" />

</template>
<style lang="less" scoped>
@import './index.less';
</style>
