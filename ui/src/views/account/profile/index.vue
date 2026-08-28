<script setup>
import Icon from '@/components/icon/Icon.vue'
import http from '@/utils/http.js'
import { formatTimeDiff } from '@/utils/time.js'
import dayjs from 'dayjs'
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'

const user = reactive({
    avatar: '',
    count: 0,
    nickname: '',
    rate: 0,
    rating: 0,
    status: 'NORMAL',
    time: 0,
    username: ''
})
const history = ref([])
const router = useRoute()
const username = router.params.username

http.post('/user/profile/' + username).then(res => {
    Object.assign(user, res)
})
http.post('/user/history', { page: 0, username }).then(res => {
    history.value = res
})

const gameResult = (item) => {
    if (item.winner === 'TIE') {
        return {
            value: 'tie',
            label: '平'
        }
    } else if (item[item.winner.toLowerCase().substring(0, 1) + 'u'].username === username) {
        return {
            value: 'victory',
            label: '胜'
        }
    } else {
        return {
            value: 'defeat',
            label: '败'
        }
    }

}
</script>

<template>
  <div class="us_profile container">
    <div class="people">
      <div class="avatar">
        <img alt="头像" :src="user.avatar ? '/api/static/avatar/' + user.avatar : '/avatar-error.jpg'"/>
      </div>
      <div class="d-flex flex-column justify-center">
        <div class="text-title-large font-weight-semibold">{{ user.nickname }}</div>
        <div class="text-body-small text-grey-darken-3 mt-1">@{{ user.username }}</div>
      </div>
      <div class="statistics">
        <div class="text-center">
          <div class="text-body-medium font-weight-medium">{{ user.rating }}</div>
          <div class="text-body-small text-grey-darken-3 mt-1">积分</div>
        </div>
        <div class="text-center">
          <div class="text-body-medium font-weight-medium">{{ user.count }}</div>
          <div class="text-body-small text-grey-darken-3 mt-1">比赛</div>
        </div>
        <div class="text-center">
          <div class="text-body-medium font-weight-medium">{{ user.rate }}%</div>
          <div class="text-body-small text-grey-darken-3 mt-1">胜率</div>
        </div>
        <div class="text-center">
          <div class="text-body-medium font-weight-medium">{{ user.time }}</div>
          <div class="text-body-small text-grey-darken-3 mt-1">游戏时长</div>
        </div>
      </div>
      <router-link to="/account">
        <v-btn
            class="text-none"
            color="blue-darken-4"
            rounded="0"
            variant="outlined"
            text="编辑资料"
        />
      </router-link>
    </div>
    <div class="history">
      <div class="title">历史对局</div>
      <div class="list">
        <div class="item" v-for="item in history" :key="item.code" :class="gameResult(item).value">
          <div class="info">
            <div class="d-flex">
              <span class="base-tag">{{
                  item.mode === 'CASUAL' ? '休闲赛' : item.mode === 'RANK' ? '积分赛' : '人机对战'
                }}</span>
              <span class="base-tag">{{
                  item.type === 'SHORT' ? `实时棋局 • ${ item.duration / 1000 / 60 }+${ item.stepDuration / 1000 }` : item.type === 'LONG' ? '通讯棋 • ' : '无限制'
                }}</span>
              <span class="base-tag">{{ `${ item.boardSize }x${ item.boardSize }` }}</span>
            </div>
            <div class="text-body-small text-grey-darken-3 text-center">
              {{ formatTimeDiff(item.endTime - item.startTime) }}
            </div>
            <div class="links">
              <Icon name="star" size="1rem" color="#F0B01A"/>
            </div>
          </div>
          <div class="versus">
            <div class="player left">
              <div class="player-name">{{ item.wu.nickname }}<span class="white-tag"/></div>
              <div class="player-rating">{{ item.wu.username }} ({{ item.wu.rating }})</div>
            </div>
            <div class="vs">{{ gameResult(item).label }}</div>
            <div class="player right">
              <div class="player-name"><span class="black-tag"/>{{ item.bu.nickname }}</div>
              <div class="player-rating">{{ item.bu.username }} ({{ item.bu.rating }})</div>
            </div>
          </div>
          <div class="secondary text-grey-darken-2">
            <div class="summer">
              <span>回合•12</span>
              <span>提子•4</span>
            </div>
            <span>{{ dayjs(item.startTime * 1000).format('YYYY-MM-DD HH:mm') }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="sass">
@use "index.sass"
</style>
