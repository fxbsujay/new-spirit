<script setup>
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import http from '@/utils/http.js'
import Icon from '@/components/icon/Icon.vue'
import dayjs from 'dayjs'
import { formatTimeDiff } from '@/utils/time.js'

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
http.post('/user/history', { page: 1, username }).then(res => {
  history.value = res
})

const gameResult = (item) => {
  if (item.winner === 'TIE') {
    return {
      value: 'tie',
      label: '平'
    }
  } else if (item[item.winner.toLowerCase()] === username) {
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
      <div class="names">
        <div class="nickname">{{ user.nickname }}</div>
        <div class="username">@{{ user.username }}</div>
      </div>
      <div class="statistics">
        <div class="item">
          <div class="value">{{ user.rating }}</div>
          <div class="name">积分</div>
        </div>
        <div class="item">
          <div class="value">{{ user.count }}</div>
          <div class="name">比赛</div>
        </div>
        <div class="item">
          <div class="value">{{ user.rate }}%</div>
          <div class="name">胜率</div>
        </div>
        <div class="item">
          <div class="value">{{ user.time }}</div>
          <div class="name">游戏时长</div>
        </div>
      </div>
      <router-link to="/account">
        <button class="button border">编辑个人资料</button>
      </router-link>
    </div>
    <div class="history">
      <div class="title">历史对局</div>
      <div class="list">
        <div class="item" v-for="item in history" :key="item.code" :class="gameResult(item).value">
          <div class="info">
            <div class="base">
              <span class="base-tag">{{
                  item.mode === 'CASUAL' ? '休闲赛' : item.mode === 'RANK' ? '积分赛' : '人机对战'
                }}</span>
              <span class="base-tag">{{
                  item.type === 'SHORT' ? `实时棋局 • ${ item.duration / 1000 / 60 }+${ item.stepDuration / 1000 }` : item.type === 'LONG' ? '通讯棋 • ' : '无限制'
                }}</span>
              <span class="base-tag">{{ `${ item.boardSize }x${ item.boardSize }` }}</span>
            </div>
            <div class="time">
              {{ formatTimeDiff(item.endTime - item.startTime) }}
            </div>
            <div class="links">
              <Icon name="star" size="1rem" color="#F0B01A"/>
            </div>
          </div>
          <div class="versus">
            <div class="player">
              <div class="player-name">{{ item.white.nickname }}<span class="white-tag"/></div>
              <div class="player-rating">{{ item.white.username }} ({{ item.white.rating }})</div>
            </div>
            <div class="vs">{{ gameResult(item).label }}</div>
            <div class="player">
              <div class="player-name"><span class="black-tag"/>{{ item.black.nickname }}</div>
              <div class="player-rating">{{ item.black.username }} ({{ item.black.rating }})</div>
            </div>
          </div>
          <div class="secondary">
            <div class="summer">
              <span>回合•12</span>
              <span>提子•4</span>
            </div>
            <span class="start-time">{{ dayjs(item.startTime * 1000).format('YYYY-MM-DD HH:mm') }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import "index.less";
</style>
