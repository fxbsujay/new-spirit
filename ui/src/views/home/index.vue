<script setup>
import Icon from '@/components/icon/Icon.vue'
import Responsive from '@/components/responsive/index.vue'
import { TypeConstant } from '@/constant/index.js'
import router from '@/router/index.js'
import { useUserStore } from '@/stores/user.js'
import http from '@/utils/http'
import { throttle } from '@/utils/index.js'
import { reactive, ref, watch } from 'vue'
import Go from '@/components/go/Go.vue'

const { user, waitGame } = useUserStore()
const games = reactive({
    page: 1,
    total: 0,
    list: []
})
const tableLoading = ref(false)

const searchHandle = throttle(() => {
    if (tableLoading.value) {
        return
    }
    tableLoading.value = true
    Object.assign(games, {
        page: 1,
        total: 0,
        list: []
    })
    http.get('/game/search', { page: games.page }).then(res => {
        Object.assign(games, res)
        tableLoading.value = false
    }).catch(() => {
        tableLoading.value = false
    })
})

searchHandle()

const tableRowClickHandle = throttle(game => {
    if (game.username === user.username) {
        return
    }
    http.post('/game/join/' + game.code).then(() => {
        router.push('/' + game.code)
    })
})

watch(waitGame, searchHandle)

const ongoingRooms = ref([])

http.get('/room/ongoing').then(res => {
    ongoingRooms.value = [...res, ...res, ...res, ...res]
})
const tab = ref(1)
</script>

<template>
  <div class="lobby-wrap container">
    <div class="lobby-side">
      <div class="">
      </div>
    </div>
    <div class="lobby-table">
      <v-tabs
          v-model="tab"
          align-tabs="center"
          color="orange-darken-4"
          grow
      >
        <v-tab fixed :value="1">快速匹配</v-tab>
        <v-tab fixed :value="2">大厅</v-tab>
        <v-tab fixed :value="3">我的</v-tab>
      </v-tabs>
      <v-tabs-window v-model="tab">
        <v-tabs-window-item :value="3">
          <v-container fluid>
            <v-row>
              <v-col v-for="item in ongoingRooms" :key="item.info.code" cols="12" md="4">
                <RouterLink :to="`/${item.info.code}`" class="text-body-small text-black">
                  <div class="d-flex justify-space-between">
                    <span>{{ item.black.nickname }}</span>
                    <span>vs</span>
                    <span>{{ item.white.nickname }}</span>
                  </div>
                  <Go :points="item.steps" :size="11" :label="false"/>
                </RouterLink>
              </v-col>
            </v-row>
          </v-container>
        </v-tabs-window-item>
        <v-tabs-window-item
            v-for="n in 2"
            :key="n"
            :value="n"
        >
          <v-container fluid>
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
              <tr v-for="item in games.list" @click="tableRowClickHandle(item)"
                  :class="item.username === user.username ? 'own-row' : ''"
                  :title="item.username === user.username ? '自己的对局' : '加入对局'">
                <td>{{ item.nickname }}</td>
                <td>{{ item.boardSize }}x{{ item.boardSize }}</td>
                <td>10h+6s</td>
                <td>{{ item.score }}</td>
                <td>{{ TypeConstant.find(type => item.type === type.value).label }}</td>
              </tr>
              </tbody>
            </table>
          </v-container>
        </v-tabs-window-item>
      </v-tabs-window>
    </div>
    <div class="lobby-play">
      <Responsive :aspect-ratio="0.5">
        <div class="banner">

        </div>
      </Responsive>
    </div>
  </div>

</template>
<style lang="less" scoped>
@import './index.less';
</style>
