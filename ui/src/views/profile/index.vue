<script setup lang="ts">
import { useUserStore } from '@/stores/user'
const { user } = useUserStore()

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
          <div class="value">2000</div>
          <div class="name">积分</div>
        </div>
        <div class="item">
          <div class="value">201</div>
          <div class="name">比赛</div>
        </div>
        <div class="item">
          <div class="value">51%</div>
          <div class="name">胜率</div>
        </div>
        <div class="item">
          <div class="value">201</div>
          <div class="name">游戏时长</div>
        </div>
      </div>
      <button class="button border">编辑个人资料</button>
    </div>
    <div class="history">
      <div class="title">历史对局</div>
      <div class="list">
        <div class="item">AAA</div>
        <div class="item">AAA</div>
        <div class="item">AAA</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import "index.less";
</style>
