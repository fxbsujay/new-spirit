
<script setup>
import Responsive from '@/components/responsive/index.vue'
import CreateDialog from './CreateDialog.vue'
import snackbar from '@/components/snackbar/index.js'
import { ref, reactive, useTemplateRef } from 'vue'
import Icon from '@/components/icon/Icon.vue'
import http from '@/utils/http'
import router from "@/router/index.js";
import { throttle } from '@/utils/index.js'
import { TypeConstant } from '@/constant/index.js'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()
const createDialogRef = useTemplateRef('createDialogRef')
const games = ref([])
const tableLoading = ref(false)

const searchHandle = throttle(() => {
  if (tableLoading.value) {
    return
  }
  tableLoading.value = true
  games.value = []
  http.get('/game/search').then(res => {
    games.value = res
    tableLoading.value = false
  }).catch(() => {
    tableLoading.value = false
  })
})

searchHandle()

const tableRowClickHandle = throttle(game => {
  if (game.username === userStore.user.username) {
    http.post('/game/cancel/').then(() => {
      searchHandle()
      snackbar.success('已取消对局')
    })
  } else {
    http.post('/game/join/' + game.code).then(() => {
      router.push('/' + game.code)
    })
  }
})

const rating = () => {

}
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
            <Icon name="search" size="14px"/>
          </div>
          <input class="search-input" type="text" placeholder="搜索房间名称或房间号"/>
        </div>
        <div class="btn-group">
          <div class="btn-icon" @click="searchHandle">
            <Icon name="refresh" size="18px"/>
          </div>
          <div class="btn-icon">
            <Icon name="settings" size="18px"/>
          </div>
        </div>
      </div>
      <table class="table">
        <thead>
        <tr>
          <td>棋手</td>
          <td>尺寸</td>
          <td>时间</td>
          <td>积分</td>
          <td>模式</td>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in games" @click="tableRowClickHandle(item)" :class="item.username === userStore.user.username ? 'own-row' : ''" :title="item.username === userStore.user.username ? '取消对局' : '加入对局'">
          <td>{{ item.nickname }}</td>
          <td>{{ item.boardSize }}x{{ item.boardSize }}</td>
          <td>10h+6s</td>
          <td>{{ item.score }}</td>
          <td>{{ TypeConstant.find(type => item.type === type.value).label }}</td>
        </tr>
        </tbody>
      </table>
    </div>
    <div class="lobby-play">
      <Responsive :aspect-ratio="0.5">
        <div class="banner">
          <img alt=""/>
        </div>
      </Responsive>
      <div class="play-btn">
        <div>

        </div>
<!--        <button class="button border" @click="createDialogRef.open()">创建游戏</button>-->
<!--        <button class="button border" @click="rating">积分赛</button>-->
<!--        <button class="button border">人机对战</button>-->
      </div>
    </div>
  </div>
  <CreateDialog  ref="createDialogRef" @createSuccess="searchHandle"/>

</template>
<style lang="less" scoped>
@import './index.less';
</style>
