<script setup>
import Responsive from '@/components/responsive/index.vue'
import Switch from '@/components/switch/index.vue'
import Go from '@/components/go/Go.vue'
import {ref} from 'vue'
import Icon from '@/components/icon/Icon.vue'

const value = ref(false)
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
          <span class="player-name">Evan Guzman</span>
          <span class="player-score">836</span>
        </div>
        <div class="player">
          <img class="player-icon" alt="黑棋选手" src="@/assets/img/b.png" />
          <span class="player-name">GouDan</span>
          <span class="player-score">1700</span>
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
          <Go />
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
        <img src="@/assets/img/b.png" class="avatar" alt="头像"/>
        <span class="username">Evan Guzman</span>
        <span class="source-label">积分 -</span>
        <span class="source-value">1742</span>
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
          <div class="step" v-for="i in 12">
            <span class="number">{{i}}</span>
            <span class="pos">a4</span>
            <span class="pos">b3</span>
          </div>
        </div>
      </div>
      <div class="user-info">
        <img src="@/assets/img/b.png" class="avatar" alt="头像"/>
        <span class="username">Evan Guzman</span>
        <span class="source-label">积分 -</span>
        <span class="source-value">1742</span>
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