
<script setup>
import Responsive from '@/components/responsive/index.vue'
import CreateDialog from './CreateDialog.vue'
import { ref, useTemplateRef } from 'vue'
import Icon from '@/components/icon/Icon.vue'
import http from '@/utils/http'

const createDialogRef = useTemplateRef('createDialogRef')

const games = ref([])

http.get('/game/search').then(res => {
  games.value = res
}).catch(() => {
  games.value = []
})

const joinGame = (code) => {
  http.post('/game/join/' + code).then(() => {
    console.log(code)
  })
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
          <div class="btn-icon">
            <Icon name="refresh" size="18px"/>
          </div>
          <div class="btn-icon">
            <Icon name="settings" size="18px"/>
          </div>
        </div>
      </div>
      <div class="lists">
        <div class="list-header">
          <div class="col name-col">棋手</div>
          <div class="col">尺寸</div>
          <div class="col">时间</div>
          <div class="col">积分</div>
          <div class="col">模式</div>
        </div>
        <div class="item" v-for="item in games" @click="joinGame(item.code)">
          <div class="col name-col">
            {{ item.nickname }}
          </div>
          <div class="col">{{ item.boardSize }}x{{ item.boardSize }}</div>
          <div class="col">10h+6s</div>
          <div class="col">1700</div>
          <div class="col">排位</div>
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
        <div>

        </div>
        <button class="button border" @click="createDialogRef.open()">创建游戏</button>
        <button class="button border">AAA</button>
        <button class="button border">积分赛</button>
      </div>
    </div>
  </div>
  <CreateDialog  ref="createDialogRef" />

</template>
<style lang="less" scoped>
@import './index.less';
</style>
