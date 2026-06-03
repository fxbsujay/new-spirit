<script setup>
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import http from '@/utils/http'

const user = reactive({
  avatar: "",
  count: 0,
  nickname: "",
  rate: 0,
  rating: 0,
  status: "NORMAL",
  time: 0,
  username: ""
})
const history = ref([])
const router = useRoute()
const username = router.params.username

http.post('/user/profile/' + username).then(res => {
  Object.assign(user, res)
})
http.post('/user/history/', { page: 1, username }).then(res => {
  history.value = res
})
</script>

<template>
  <div class="us_profile container">
    <div class="people">
      <div class="avatar">
        <img alt="头像" :src="user.avatar"/>
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
      <button class="button border">编辑个人资料</button>
    </div>
    <div class="history">
      <div class="title">历史对局</div>
      <div class="list">
        <div class="item" v-for="item in history" :key="item.code">
          <span>{{ item.white }} VS {{ item.black }}</span>
        </div>
        <div class="item">AAA</div>
        <div class="item">AAA</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import "index.less";
</style>
