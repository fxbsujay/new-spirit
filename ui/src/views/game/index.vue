<script setup>
import Responsive from '@/components/responsive/index.vue'
import Switch from '@/components/switch/index.vue'
import Go from '@/components/go/Go.vue'
import { ref } from 'vue'
import Icon from '@/components/icon/Icon.vue'
import { useRoute } from 'vue-router'
import { GameSocket } from './index'
import { useUserStore } from '@/stores/user.js'
import {PRETTY_COORDINATE_SEQUENCE} from "@/components/go/goban.js";

const value = ref(false)
const router = useRoute()
const userStore = useUserStore()
const socket = new GameSocket(router.params.code)

const { game, loading, success } = socket

const onBoardClick = (x, y) => {
  socket.addStep(x, y)
}

</script>

<template>
  <div class="main-container">
    <div class="side">
      <div class="card">
        <div class="game-info">
          <img class="mode-icon" src="@/assets/img/game-rank.png" alt="排位">
          <div class="info">
            <div>10h+6s • 休闲 • 通讯棋 </div>
            <div>9分钟前</div>
          </div>
          <Icon name="star" size="1.5rem" color="#F0B01A"/>
        </div>
        <div class="player">
          <img class="player-icon" alt="白棋选手" src="@/assets/img/w.png" />
          <span class="player-name">{{ game.white.nickname }}</span>
          <span class="player-score">{{ game.white.rating }}</span>
        </div>
        <div class="player">
          <img class="player-icon" alt="黑棋选手" src="@/assets/img/b.png" />
          <span class="player-name">{{ game.black.nickname }}</span>
          <span class="player-score">{{ game.black.rating }}</span>
        </div>
      </div>
      <div class="chat-wrap">
        <div class="header card">
          <span>聊天室</span>
          <Switch v-model="value"/>
        </div>
        <div class="message-box card">
          <div class="message-item">
            <img src="@/assets/img/w.png" class="avatar" alt="头像"/>
            <div class="info">
              <div class="username">Evan Guzman</div>
              <div class="content">
                <span class="tag" />
                <span class="text">send message send12222222222222222222222133432123123124 </span>
              </div>
            </div>
          </div>

          <div class="message-item receive">
            <img src="@/assets/img/b.png" class="avatar" alt="头像"/>
            <div class="info">
              <div class="username">Gou Dan</div>
              <div class="content">
                <span class="tag" />
                <span class="text">Send message send12222222222222222222222133432123123124</span>
              </div>
            </div>
          </div>
        </div>
        <div class="send-input">
          <input class="input" placeholder="恶语伤人六月寒..."/>
          <div class="icon-box">
            <Icon name="send" size="1rem" color="#fff"/>
          </div>
        </div>
      </div>
    </div>
    <div class="board">
      <Responsive :aspect-ratio="1">
        <div class="A">
          <Go :onBoardClick="onBoardClick" :points="game.steps"/>
        </div>
      </Responsive>
    </div>
    <div class="side controller-side">
      <div class="game-time">
        <div class="time">
          <time>15:32</time>
        </div>
        <Icon name="signal" size="20px" color="#F0B01A"/>
        <div class="icon-box">
          <Icon name="plus" size="1rem" color="#fff"/>
        </div>
      </div>
      <div class="time-progress"></div>
      <div class="user-info">
        <img src="@/assets/img/w.png" class="avatar" alt="白棋玩家"/>
        <span class="username">{{ userStore.user.username === game.info.white ? game.black.nickname : game.white.nickname }}</span>
        <span class="source-label">积分 -</span>
        <span class="source-value">{{ userStore.user.username === game.info.white ? game.black.rating : game.white.rating }}</span>
      </div>
      <div class="controller">
        <div class="buttons">
          <div class="btn"><Icon name="video"/></div>
          <div class="btn"><Icon name="skip-back"/></div>
          <div class="btn"><Icon name="skip-previous"/></div>
          <div class="btn"><Icon name="skip-next"/></div>
          <div class="btn"><Icon name="skip-for"/></div>
          <div class="btn"><Icon name="menu"/></div>
        </div>
        <div class="step-wrap">
          <div class="step" v-for="index in Math.ceil(game.steps.length / 2)">
            <span class="number">{{index}}</span>
            <span class="pos">{{PRETTY_COORDINATE_SEQUENCE[game.steps[(index - 1) * 2].x]}}{{ game.steps[(index - 1) * 2].y + 1}}</span>
            <span class="pos" v-if="game.steps[index * 2 - 1]">{{PRETTY_COORDINATE_SEQUENCE[game.steps[index * 2 - 1].x]}}{{ game.steps[index * 2 - 1].y + 1}}</span>
          </div>
        </div>
      </div>
      <div class="user-info">
        <img src="@/assets/img/b.png" class="avatar" alt="黑棋玩家"/>
        <span class="username">{{ userStore.user.username === game.info.white ? game.white.nickname : game.black.nickname }}</span>
        <span class="source-label">积分 -</span>
        <span class="source-value">{{ userStore.user.username === game.info.white ? game.white.rating : game.black.rating }}</span>
      </div>
      <div class="time-progress"></div>
      <div class="game-time">
        <div class="time">
          <time>15:32</time>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import "./index.less";
</style>